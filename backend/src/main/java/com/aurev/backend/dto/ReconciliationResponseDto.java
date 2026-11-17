package com.aurev.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ReconciliationResponseDto(
        String noticeId,
        Long clientId,
        int totalMismatches,
        BigDecimal totalItcAtRisk,
        Map<String, BigDecimal> categoryBreakdown,
        List<MismatchRowDto> annexureRows
) {}
