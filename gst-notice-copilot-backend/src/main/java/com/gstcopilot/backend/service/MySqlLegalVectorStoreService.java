package com.gstcopilot.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gstcopilot.backend.entity.LegalProvision;
import com.gstcopilot.backend.repository.LegalProvisionRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * MySQL 8 replacement for PgVectorStore.
 * Computes semantic cosine similarity over JSON float vectors stored in MySQL `legal_provisions.embedding_json`
 * whenever an EmbeddingModel (e.g. Ollama nomic-embed-text) is active, and seamlessly falls back to MySQL 8
 * native FULLTEXT natural language search (`MATCH ... AGAINST`) so the pipeline works with zero PostgreSQL dependency.
 */
@Service
public class MySqlLegalVectorStoreService {

    private final LegalProvisionRepository legalProvisionRepository;
    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final ObjectMapper objectMapper;

    public MySqlLegalVectorStoreService(
            LegalProvisionRepository legalProvisionRepository,
            ObjectProvider<EmbeddingModel> embeddingModelProvider,
            ObjectMapper objectMapper) {
        this.legalProvisionRepository = legalProvisionRepository;
        this.embeddingModelProvider = embeddingModelProvider;
        this.objectMapper = objectMapper;
    }

    public List<LegalProvision> retrieveTopK(String sanitizedQuery, String formType, int topK) {
        EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
        if (embeddingModel != null) {
            try {
                float[] queryVector = embeddingModel.embed(sanitizedQuery);
                List<LegalProvision> allProvisions = legalProvisionRepository.findAll();

                List<ScoredProvision> scored = allProvisions.stream()
                        .map(provision -> new ScoredProvision(
                                provision,
                                scoreWithEmbedding(provision, queryVector, embeddingModel, formType)
                        ))
                        .sorted(Comparator.comparingDouble((ScoredProvision provision) -> provision.score()).reversed())
                        .limit(topK)
                        .toList();

                if (!scored.isEmpty()) {
                    return scored.stream().map(scoredProvision -> scoredProvision.provision()).toList();
                }
            } catch (Exception ignored) {
                // Fall through cleanly to MySQL FULLTEXT search if embedding endpoint is unavailable
            }
        }

        // MySQL 8 Native FULLTEXT natural language retrieval
        List<LegalProvision> fullTextMatches =
                legalProvisionRepository.searchFullTextTopK(sanitizedQuery, formType, topK);

        if (fullTextMatches.size() < topK) {
            return legalProvisionRepository.findAll().stream()
                    .sorted(Comparator.comparingInt((LegalProvision lp) ->
                            lp.getApplicableForms().contains(formType) ? 0 : 1))
                    .limit(topK)
                    .toList();
        }
        return fullTextMatches;
    }

    private double scoreWithEmbedding(
            LegalProvision provision,
            float[] queryVector,
            EmbeddingModel embeddingModel,
            String formType) {
        float[] docVector = loadOrComputeVector(provision, embeddingModel);
        double similarity = cosineSimilarity(queryVector, docVector);
        double formBoost = provision.getApplicableForms() != null
                && provision.getApplicableForms().contains(formType) ? 0.12 : 0.0;
        return similarity + formBoost;
    }

    private float[] loadOrComputeVector(LegalProvision provision, EmbeddingModel embeddingModel) {
        if (provision.getEmbeddingJson() != null && !provision.getEmbeddingJson().isBlank()) {
            try {
                List<Double> stored = objectMapper.readValue(
                        provision.getEmbeddingJson(), new TypeReference<>() {});
                float[] arr = new float[stored.size()];
                for (int i = 0; i < stored.size(); i++) {
                    arr[i] = stored.get(i).floatValue();
                }
                return arr;
            } catch (Exception ignored) {
                // Recompute embedding below if malformed
            }
        }
        float[] computed = embeddingModel.embed(
                provision.getTitle() + " " + provision.getSectionRef() + " " + provision.getFullText());
        try {
            provision.setEmbeddingJson(objectMapper.writeValueAsString(computed));
            legalProvisionRepository.save(provision);
        } catch (Exception ignored) {
            // Non-fatal cache write
        }
        return computed;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length == 0 || a.length != b.length) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0.0 ? 0.0 : dot / denom;
    }

    private record ScoredProvision(LegalProvision provision, double score) {
    }
}
