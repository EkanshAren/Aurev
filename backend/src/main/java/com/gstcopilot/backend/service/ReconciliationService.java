package com.gstcopilot.backend.service;

import com.gstcopilot.backend.dto.ReconciliationItemDto;
import com.gstcopilot.backend.dto.ReconciliationResponseDto;
import com.gstcopilot.backend.entity.GstNotice;
import com.gstcopilot.backend.repository.GstNoticeRepository;
import com.gstcopilot.backend.repository.PurchaseRegisterInvoiceRepository;
import com.gstcopilot.backend.repository.PurchaseRegisterInvoiceRepository.ReconciliationRowProjection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SQL-First Reconciliation Service backed by MySQL 8 FULL OUTER JOIN emulation
 * (LEFT JOIN ... UNION ALL ... RIGHT JOIN). All financial math uses exact BigDecimal.
 */
@Service
public class ReconciliationService {

    private final GstNoticeRepository noticeRepository;
    private final PurchaseRegisterInvoiceRepository purchaseRepository;

    public ReconciliationService(
            GstNoticeRepository noticeRepository,
            PurchaseRegisterInvoiceRepository purchaseRepository) {
        this.noticeRepository = noticeRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional(readOnly = true)
    public ReconciliationResponseDto reconcile(Long noticeId) {
        GstNotice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("GST Notice not found for id=" + noticeId));

        List<ReconciliationRowProjection> rows = purchaseRepository.reconcileInvoicesByNoticeId(noticeId);

        BigDecimal total2b = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalBooks = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        int matched = 0;
        int missing2b = 0;
        int missingBooks = 0;
        int mismatch = 0;

        List<ReconciliationItemDto> items = new ArrayList<>(rows.size());
        for (ReconciliationRowProjection row : rows) {
            BigDecimal gstr2bItc = safeScale(row.getGstr2bItc());
            BigDecimal booksItc = safeScale(row.getBooksItc());
            BigDecimal diffItc = safeScale(row.getDifferenceItc());
            String status = row.getStatus();

            total2b = total2b.add(gstr2bItc);
            totalBooks = totalBooks.add(booksItc);

            switch (status) {
                case "MATCHED" -> matched++;
                case "MISSING_IN_2B" -> missing2b++;
                case "MISSING_IN_BOOKS" -> missingBooks++;
                default -> mismatch++;
            }

            items.add(new ReconciliationItemDto(
                    row.getSupplierGstin(),
                    row.getSupplierName(),
                    row.getInvoiceNumber(),
                    row.getInvoiceDate(),
                    gstr2bItc,
                    booksItc,
                    diffItc,
                    status
            ));
        }

        BigDecimal netDiff = totalBooks.subtract(total2b).setScale(2, RoundingMode.HALF_UP);

        return new ReconciliationResponseDto(
                notice.getId(),
                notice.getDin(),
                notice.getTaxpayerGstin(),
                notice.getFormType(),
                total2b,
                totalBooks,
                netDiff,
                matched,
                missing2b,
                missingBooks,
                mismatch,
                items
        );
    }

    private BigDecimal safeScale(BigDecimal value) {
        return (value != null ? value : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
