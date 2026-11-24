package com.gstcopilot.backend.dto;

import java.util.List;

/**
 * File 4: Locked JSON contract for POST /api/notices/{id}/draft.
 * Exact required keys: draftText, verifiedCitations, maskedTokens.
 */
public record DraftReplyResponseDto(
        String draftText,
        List<VerifiedCitation> verifiedCitations,
        List<MaskedTokenAudit> maskedTokens
) {
    public record VerifiedCitation(
            String citationId,
            String sectionRef,
            String title,
            boolean verified
    ) {
    }

    public record MaskedTokenAudit(
            String placeholder,
            String category,
            String maskedPreview
    ) {
    }
}
