package com.aurev.backend.service;

import com.aurev.backend.dto.MismatchRowDto;
import com.aurev.backend.dto.ReconciliationResponseDto;
import com.aurev.backend.model.GstNotice;
import com.aurev.backend.repository.GstNoticeRepository;
import com.aurev.backend.repository.ReconciliationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReconciliationService {

    private final GstNoticeRepository noticeRepo;
    private final ReconciliationRepository reconciliationRepo;

    public ReconciliationService(GstNoticeRepository noticeRepo,
                                 ReconciliationRepository reconciliationRepo) {
        this.noticeRepo = noticeRepo;
        this.reconciliationRepo = reconciliationRepo;
    }

    @Transactional
    public ReconciliationResponseDto reconcileNotice(String noticeId) {
        GstNotice notice = noticeRepo.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("Notice not found: " + noticeId));

        Long clientId = notice.getClient().getId();
        List<MismatchRowDto> mismatches = reconciliationRepo.findMismatchesByClientId(clientId);

        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        breakdown.put("MISSING_IN_2B", BigDecimal.ZERO);
        breakdown.put("VALUE_MISMATCH", BigDecimal.ZERO);
        breakdown.put("BLOCKED_OR_INELIGIBLE", BigDecimal.ZERO);
        breakdown.put("MISSING_IN_BOOKS", BigDecimal.ZERO);

        BigDecimal totalAtRisk = BigDecimal.ZERO;
        for (MismatchRowDto row : mismatches) {
            BigDecimal risk = "BLOCKED_OR_INELIGIBLE".equals(row.mismatchType())
                    ? row.booksTax()
                    : row.taxDifference().abs();
            breakdown.merge(row.mismatchType(), risk, BigDecimal::add);
            if (!"MISSING_IN_BOOKS".equals(row.mismatchType())) {
                totalAtRisk = totalAtRisk.add(risk);
            }
        }

        notice.setStatus("RECONCILED");
        noticeRepo.save(notice);

        return new ReconciliationResponseDto(
                noticeId,
                clientId,
                mismatches.size(),
                totalAtRisk,
                breakdown,
                mismatches
        );
    }
}
