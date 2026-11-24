package com.gstcopilot.backend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response contract for GET /api/notices/{id}/reconcile.
 */
public record ReconciliationResponseDto(
        Long noticeId,
        String din,
        String taxpayerGstin,
        String formType,
        BigDecimal totalGstr2bItc,
        BigDecimal totalBooksItc,
        BigDecimal netDifferenceItc,
        int matchedCount,
        int missingIn2bCount,
        int missingInBooksCount,
        int amountMismatchCount,
        List<ReconciliationItemDto> items
) {
}
