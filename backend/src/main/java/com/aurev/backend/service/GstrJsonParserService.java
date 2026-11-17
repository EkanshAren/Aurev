package com.aurev.backend.service;

import com.aurev.backend.model.Gstr2bInvoice;
import com.aurev.backend.model.PurchaseLedger;
import com.aurev.backend.repository.Gstr2bInvoiceRepository;
import com.aurev.backend.repository.PurchaseLedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class GstrJsonParserService {

    private final Gstr2bInvoiceRepository gstr2bRepo;
    private final PurchaseLedgerRepository ledgerRepo;
    private final ObjectMapper objectMapper;

    public GstrJsonParserService(Gstr2bInvoiceRepository gstr2bRepo,
                                 PurchaseLedgerRepository ledgerRepo,
                                 ObjectMapper objectMapper) {
        this.gstr2bRepo = gstr2bRepo;
        this.ledgerRepo = ledgerRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public int ingestGstr2bJson(Long clientId, InputStream jsonStream) {
        List> items = objectMapper.readValue(jsonStream, new TypeReference<>() {});
        gstr2bRepo.deleteByClientId(clientId);

        for (Map<String, Object> item : items) {
            Gstr2bInvoice inv = new Gstr2bInvoice();
            inv.setClientId(clientId);
            inv.setSupplierGstin(String.valueOf(item.get("supplierGstin")));
            inv.setSupplierName(String.valueOf(item.getOrDefault("supplierName", "Unknown Supplier")));
            inv.setInvoiceNumber(String.valueOf(item.get("invoiceNumber")));
            inv.setInvoiceDate(LocalDate.parse(String.valueOf(item.get("invoiceDate"))));
            inv.setTaxableValue(new BigDecimal(String.valueOf(item.getOrDefault("taxableValue", "0"))));
            inv.setIgst(new BigDecimal(String.valueOf(item.getOrDefault("igst", "0"))));
            inv.setCgst(new BigDecimal(String.valueOf(item.getOrDefault("cgst", "0"))));
            inv.setSgst(new BigDecimal(String.valueOf(item.getOrDefault("sgst", "0"))));
            inv.setItcAvailable(Boolean.parseBoolean(String.valueOf(item.getOrDefault("itcAvailable", "true"))));
            inv.setFilingStatus(String.valueOf(item.getOrDefault("filingStatus", "FILED")));
            gstr2bRepo.save(inv);
        }
        return items.size();
    }

    @Transactional
    public int ingestPurchaseLedgerJson(Long clientId, InputStream jsonStream) {
        List> items = objectMapper.readValue(jsonStream, new TypeReference<>() {});
        ledgerRepo.deleteByClientId(clientId);

        for (Map<String, Object> item : items) {
            PurchaseLedger row = new PurchaseLedger();
            row.setClientId(clientId);
            row.setSupplierGstin(String.valueOf(item.get("supplierGstin")));
            row.setSupplierName(String.valueOf(item.getOrDefault("supplierName", "Unknown Supplier")));
            row.setInvoiceNumber(String.valueOf(item.get("invoiceNumber")));
            row.setInvoiceDate(LocalDate.parse(String.valueOf(item.get("invoiceDate"))));
            row.setTaxableValue(new BigDecimal(String.valueOf(item.getOrDefault("taxableValue", "0"))));
            BigDecimal igst = new BigDecimal(String.valueOf(item.getOrDefault("igst", "0")));
            BigDecimal cgst = new BigDecimal(String.valueOf(item.getOrDefault("cgst", "0")));
            BigDecimal sgst = new BigDecimal(String.valueOf(item.getOrDefault("sgst", "0")));
            row.setIgst(igst);
            row.setCgst(cgst);
            row.setSgst(sgst);
            row.setItcClaimed(igst.add(cgst).add(sgst));
            ledgerRepo.save(row);
        }
        return items.size();
    }
}
