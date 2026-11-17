package com.aurev.backend.repository;

import com.aurev.backend.dto.MismatchRowDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReconciliationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<MismatchRowDto> findMismatchesByClientId(Long clientId) {
        String sql = """
            SELECT
                p.supplier_gstin AS supplier_gstin,
                COALESCE(p.supplier_name, g.supplier_name, 'Unknown Supplier') AS supplier_name,
                p.invoice_number AS invoice_number,
                CAST(p.invoice_date AS CHAR) AS invoice_date,
                p.taxable_value AS books_taxable,
                (p.igst + p.cgst + p.sgst) AS books_tax,
                COALESCE(g.taxable_value, 0.00) AS gstr2b_taxable,
                COALESCE(g.igst + g.cgst + g.sgst, 0.00) AS gstr2b_tax,
                ((p.igst + p.cgst + p.sgst) - COALESCE(g.igst + g.cgst + g.sgst, 0.00)) AS tax_diff,
                CASE
                    WHEN g.id IS NULL THEN 'MISSING_IN_2B'
                    WHEN ABS(p.igst - g.igst) > 1.00 OR ABS(p.cgst - g.cgst) > 1.00 OR ABS(p.sgst - g.sgst) > 1.00 THEN 'VALUE_MISMATCH'
                    WHEN g.itc_available = FALSE THEN 'BLOCKED_OR_INELIGIBLE'
                    ELSE 'MATCHED'
                END AS mismatch_type
            FROM purchase_ledger p
            LEFT JOIN gstr2b_invoices g
                ON p.client_id = g.client_id
                AND p.supplier_gstin = g.supplier_gstin
                AND p.invoice_number = g.invoice_number
            WHERE p.client_id = :clientId
              AND (
                  g.id IS NULL
                  OR ABS(p.igst - g.igst) > 1.00
                  OR ABS(p.cgst - g.cgst) > 1.00
                  OR ABS(p.sgst - g.sgst) > 1.00
                  OR g.itc_available = FALSE
              )
            UNION ALL
            SELECT
                g.supplier_gstin AS supplier_gstin,
                COALESCE(g.supplier_name, 'Unknown Supplier') AS supplier_name,
                g.invoice_number AS invoice_number,
                CAST(g.invoice_date AS CHAR) AS invoice_date,
                0.00 AS books_taxable,
                0.00 AS books_tax,
                g.taxable_value AS gstr2b_taxable,
                (g.igst + g.cgst + g.sgst) AS gstr2b_tax,
                -(g.igst + g.cgst + g.sgst) AS tax_diff,
                'MISSING_IN_BOOKS' AS mismatch_type
            FROM purchase_ledger p
            RIGHT JOIN gstr2b_invoices g
                ON p.client_id = g.client_id
                AND p.supplier_gstin = g.supplier_gstin
                AND p.invoice_number = g.invoice_number
            WHERE g.client_id = :clientId
              AND p.id IS NULL
            """;

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("clientId", clientId)
                .getResultList();

        List<MismatchRowDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(new MismatchRowDto(
                    String.valueOf(r[0]),
                    String.valueOf(r[1]),
                    String.valueOf(r[2]),
                    String.valueOf(r[3]),
                    toBigDecimal(r[4]),
                    toBigDecimal(r[5]),
                    toBigDecimal(r[6]),
                    toBigDecimal(r[7]),
                    toBigDecimal(r[8]),
                    String.valueOf(r[9])
            ));
        }
        return result;
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        return new BigDecimal(val.toString());
    }
}
