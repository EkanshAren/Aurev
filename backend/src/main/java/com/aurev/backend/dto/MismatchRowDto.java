package com.aurev.backend.dto;

import java.math.BigDecimal;

public record MismatchRowDto(
        String supplierGstin,
        String supplierName,
        String invoiceNumber,
        String invoiceDate,
        BigDecimal booksTaxable,
        BigDecimal booksTax,
        BigDecimal gstr2bTaxable,
        BigDecimal gstr2bTax,
        BigDecimal taxDifference,
        String mismatchType
) {}
