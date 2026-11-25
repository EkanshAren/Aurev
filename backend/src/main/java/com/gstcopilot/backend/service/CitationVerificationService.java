package com.gstcopilot.backend.service;

import com.gstcopilot.backend.dto.DraftReplyResponseDto.VerifiedCitation;
import com.gstcopilot.backend.entity.LegalProvision;
import com.gstcopilot.backend.repository.LegalProvisionRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Verifies every [CITATION_ID] tag produced by the LLM against the MySQL `legal_provisions` table.
 * Unverified or hallucinated citation tags are flagged explicitly to prevent ungrounded legal filings.
 */
@Service
public class CitationVerificationService {

    private static final Pattern CITATION_TAG_PATTERN =
            Pattern.compile("\\[([A-Z0-9_]{5,64})\\]");

    private static final Set<String> IGNORED_MASK_TAGS = Set.of(
            "TAXPAYER_GSTIN",
            "CLIENT_ENTITY",
            "PAN_MASKED"
    );

    private final LegalProvisionRepository legalProvisionRepository;

    public CitationVerificationService(LegalProvisionRepository legalProvisionRepository) {
        this.legalProvisionRepository = legalProvisionRepository;
    }

    public List<VerifiedCitation> verifyCitations(String draftText) {
        if (draftText == null || draftText.isBlank()) {
            return List.of();
        }

        Set<String> extractedIds = new LinkedHashSet<>();
        Matcher matcher = CITATION_TAG_PATTERN.matcher(draftText);
        while (matcher.find()) {
            String candidate = matcher.group(1);
            if (!isMaskPlaceholder(candidate)) {
                extractedIds.add(candidate);
            }
        }

        if (extractedIds.isEmpty()) {
            return List.of();
        }

        Map<String, LegalProvision> dbMap = legalProvisionRepository.findByCitationIdIn(extractedIds)
                .stream()
            .collect(Collectors.toMap(provision -> provision.getCitationId(), Function.identity()));

        List<VerifiedCitation> verifiedList = new ArrayList<>(extractedIds.size());
        for (String id : extractedIds) {
            LegalProvision match = dbMap.get(id);
            if (match != null) {
                verifiedList.add(new VerifiedCitation(
                        match.getCitationId(),
                        match.getSectionRef(),
                        match.getTitle(),
                        true
                ));
            } else {
                verifiedList.add(new VerifiedCitation(
                        id,
                        "Unverified Citation — Manual CA Review Required",
                        "Not found in MySQL legal_provisions corpus",
                        false
                ));
            }
        }
        return verifiedList;
    }

    private boolean isMaskPlaceholder(String token) {
        return IGNORED_MASK_TAGS.contains(token)
                || token.startsWith("VENDOR_GSTIN_")
                || token.startsWith("PAN_MASKED_");
    }
}
