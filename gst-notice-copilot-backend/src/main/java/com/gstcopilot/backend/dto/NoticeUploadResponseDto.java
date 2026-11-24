package com.gstcopilot.backend.dto;

import java.math.BigDecimal;

/**
 * Locked JSON contract for POST /api/notices/upload:
 * {"noticeId": 1, "din": "202608...", "gstin": "29AAB...", "demandedItc": 145000.00}
 */
public record NoticeUploadResponseDto(
        Long noticeId,
        String din,
        String gstin,
        BigDecimal demandedItc
) {
}
