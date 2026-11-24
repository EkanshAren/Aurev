package com.gstcopilot.backend.dto;

import java.math.BigDecimal;

public record ReconciliationItemDto(
        String supplierGstin,
        String supplierName,
        String invoiceNumber,
        String invoiceDate,
        BigDecimal gstr2bItc,
        BigDecimal booksItc,
        BigDecimal differenceItc,
        String status
) {
}
