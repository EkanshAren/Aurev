package com.aurev.backend.service;

import com.aurev.backend.model.LegalCitation;
import com.aurev.backend.repository.LegalCitationRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CitationVerificationService {

    private static final Pattern CITATION_PATTERN = Pattern.compile("\\[(CGST-[A-Z0-9-]+|CBIC-[A-Z0-9-]+)]");

    private final LegalCitationRepository citationRepo;

    public CitationVerificationService(LegalCitationRepository citationRepo) {
        this.citationRepo = citationRepo;
    }

    public List<LegalCitation> verifyAndFetchCitations(String draftText) {
        Set<String> extractedIds = new LinkedHashSet<>();
        Matcher matcher = CITATION_PATTERN.matcher(draftText == null ? "" : draftText);
        while (matcher.find()) {
            extractedIds.add(matcher.group(1));
        }
        return citationRepo.findAllById(extractedIds);
    }

    public String stripUnverifiedCitations(String draftText) {
        if (draftText == null) return "";
        Matcher matcher = CITATION_PATTERN.matcher(draftText);
        StringBuilder cleaned = new StringBuilder();
        while (matcher.find()) {
            String id = matcher.group(1);
            String replacement = citationRepo.existsById(id) ? "[" + id + "]" : "[UNVERIFIED-CITATION-REMOVED]";
            matcher.appendReplacement(cleaned, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(cleaned);
        return cleaned.toString();
    }
}
