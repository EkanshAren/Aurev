package com.gstcopilot.backend.repository;

import com.gstcopilot.backend.entity.PurchaseRegisterInvoice;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRegisterInvoiceRepository extends JpaRepository<PurchaseRegisterInvoice, Long> {

    List<PurchaseRegisterInvoice> findByNoticeId(Long noticeId);

    void deleteByNoticeId(Long noticeId);

    interface ReconciliationRowProjection {
        String getSupplierGstin();
        String getSupplierName();
        String getInvoiceNumber();
        String getInvoiceDate();
        BigDecimal getGstr2bItc();
        BigDecimal getBooksItc();
        BigDecimal getDifferenceItc();
        String getStatus();
    }

    /**
     * Native MySQL 8 FULL OUTER JOIN emulation using LEFT JOIN UNION ALL RIGHT JOIN.
     * MySQL does not support PostgreSQL's FULL OUTER JOIN syntax directly; this parameterized
     * SQL-First query guarantees exact invoice-level reconciliation across GSTR-2B and Purchase Books.
     */
    @Query(value = """
            SELECT
                p.supplier_gstin AS supplierGstin,
                COALESCE(p.supplier_name, g.supplier_name, 'Unknown Supplier') AS supplierName,
                p.invoice_number AS invoiceNumber,
                COALESCE(p.invoice_date, g.invoice_date, '') AS invoiceDate,
                COALESCE(g.total_itc, 0.00) AS gstr2bItc,
                COALESCE(p.total_itc, 0.00) AS booksItc,
                (COALESCE(p.total_itc, 0.00) - COALESCE(g.total_itc, 0.00)) AS differenceItc,
                CASE
                    WHEN g.id IS NULL THEN 'MISSING_IN_2B'
                    WHEN ABS(p.total_itc - g.total_itc) <= 1.00 THEN 'MATCHED'
                    ELSE 'AMOUNT_MISMATCH'
                END AS status
            FROM purchase_register_invoices p
            LEFT JOIN gstr2b_invoices g
                ON p.notice_id = g.notice_id
                AND UPPER(TRIM(p.supplier_gstin)) = UPPER(TRIM(g.supplier_gstin))
                AND UPPER(TRIM(p.invoice_number)) = UPPER(TRIM(g.invoice_number))
            WHERE p.notice_id = :noticeId

            UNION ALL

            SELECT
                g.supplier_gstin AS supplierGstin,
                COALESCE(g.supplier_name, 'Unknown Supplier') AS supplierName,
                g.invoice_number AS invoiceNumber,
                COALESCE(g.invoice_date, '') AS invoiceDate,
                COALESCE(g.total_itc, 0.00) AS gstr2bItc,
                0.00 AS booksItc,
                (0.00 - COALESCE(g.total_itc, 0.00)) AS differenceItc,
                'MISSING_IN_BOOKS' AS status
            FROM gstr2b_invoices g
            LEFT JOIN purchase_register_invoices p
                ON g.notice_id = p.notice_id
                AND UPPER(TRIM(g.supplier_gstin)) = UPPER(TRIM(p.supplier_gstin))
                AND UPPER(TRIM(g.invoice_number)) = UPPER(TRIM(p.invoice_number))
            WHERE g.notice_id = :noticeId
              AND p.id IS NULL
            ORDER BY supplierGstin, invoiceNumber
            """, nativeQuery = true)
    List<ReconciliationRowProjection> reconcileInvoicesByNoticeId(@Param("noticeId") Long noticeId);
}
