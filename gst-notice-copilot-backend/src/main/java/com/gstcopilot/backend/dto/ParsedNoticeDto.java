package com.gstcopilot.backend.dto;

import java.math.BigDecimal;

/**
 * File 3: Extracted metadata from an uploaded GST notice PDF (ASMT-10, DRC-01, REG-17).
 */
public record ParsedNoticeDto(
        String din,
        String gstin,
        String taxpayerName,
        String formType,
        String taxPeriod,
        String dueDate,
        BigDecimal demandedItc,
        String rawText
) {
}
