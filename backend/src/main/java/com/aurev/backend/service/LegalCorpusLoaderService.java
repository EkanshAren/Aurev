package com.aurev.backend.service;

import com.aurev.backend.model.LegalCitation;
import com.aurev.backend.repository.LegalCitationRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class LegalCorpusLoaderService {

    private static final Logger log = LoggerFactory.getLogger(LegalCorpusLoaderService.class);

    private final LegalCitationRepository citationRepo;
    private final ObjectMapper objectMapper;
    private SimpleVectorStore vectorStore;

    public LegalCorpusLoaderService(LegalCitationRepository citationRepo, ObjectMapper objectMapper) {
        this.citationRepo = citationRepo;
        this.objectMapper = objectMapper;
    }

    @Bean
    public VectorStore vectorStore(@Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        return this.vectorStore;
    }

    @PostConstruct
    public void loadCorpusOnStartup() {
        try (InputStream is = new ClassPathResource("legal_corpus.json").getInputStream()) {
            List> entries = objectMapper.readValue(is, new TypeReference<>() {});
            List<Document> docs = new ArrayList<>();

            for (Map<String, String> e : entries) {
                LegalCitation citation = new LegalCitation();
                citation.setCitationId(e.get("citationId"));
                citation.setActOrCircular(e.get("actOrCircular"));
                citation.setSectionRef(e.get("sectionRef"));
                citation.setTitle(e.get("title"));
                citation.setSummary(e.get("summary"));
                if (e.get("effectiveFrom") != null) {
                    citation.setEffectiveFrom(LocalDate.parse(e.get("effectiveFrom")));
                }
                citationRepo.save(citation);

                String content = "[" + citation.getCitationId() + "] " + citation.getActOrCircular()
                        + " - " + citation.getSectionRef() + ": " + citation.getTitle()
                        + ". " + citation.getSummary();
                docs.add(new Document(content, Map.of(
                        "citationId", citation.getCitationId(),
                        "sectionRef", citation.getSectionRef()
                )));
            }

            if (vectorStore != null && !docs.isEmpty()) {
                vectorStore.add(docs);
                log.info("Loaded {} verified GST legal citations into MySQL and SimpleVectorStore.", docs.size());
            }
        } catch (Exception ex) {
            log.warn("Legal corpus vector indexing skipped (SQL corpus still active): {}", ex.getMessage());
        }
    }
}
