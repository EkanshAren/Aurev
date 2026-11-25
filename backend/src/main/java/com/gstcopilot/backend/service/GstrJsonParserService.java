package com.gstcopilot.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gstcopilot.backend.entity.GstNotice;
import com.gstcopilot.backend.entity.Gstr2bInvoice;
import com.gstcopilot.backend.entity.PurchaseRegisterInvoice;
import com.gstcopilot.backend.repository.Gstr2bInvoiceRepository;
import com.gstcopilot.backend.repository.PurchaseRegisterInvoiceRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Parses official GSTN Form GSTR-2B JSON and CA Purchase Register JSON into MySQL 3NF tables.
 */
@Service
public class GstrJsonParserService {

    private final ObjectMapper objectMapper;
    private final Gstr2bInvoiceRepository gstr2bRepository;
    private final PurchaseRegisterInvoiceRepository purchaseRepository;

    public GstrJsonParserService(
            ObjectMapper objectMapper,
            Gstr2bInvoiceRepository gstr2bRepository,
            PurchaseRegisterInvoiceRepository purchaseRepository) {
        this.objectMapper = objectMapper;
        this.gstr2bRepository = gstr2bRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional
    public int parseAndSaveGstr2b(GstNotice notice, byte[] gstr2bJsonBytes) {
        if (gstr2bJsonBytes == null || gstr2bJsonBytes.length == 0) {
            return 0;
        }
        try {
            JsonNode root = objectMapper.readTree(gstr2bJsonBytes);
            List<Gstr2bInvoice> entities = new ArrayList<>();

            // Support official GSTN nested shape: data.docdata.b2b[].inv[]
            JsonNode b2bArray = root.path("data").path("docdata").path("b2b");
            if (b2bArray.isArray()) {
                for (JsonNode supplierNode : b2bArray) {
                    String ctin = textOrEmpty(supplierNode, "ctin").toUpperCase(Locale.ROOT);
                    String trdnm = textOrEmpty(supplierNode, "trdnm");
                    JsonNode invoices = supplierNode.path("inv");
                    if (invoices.isArray()) {
                        for (JsonNode inv : invoices) {
                            entities.add(mapGstr2bInvoice(notice, ctin, trdnm, inv));
                        }
                    }
                }
            } else if (root.isArray() || root.path("invoices").isArray()) {
                // Also support flattened invoice array format
                JsonNode flatArray = root.isArray() ? root : root.path("invoices");
                for (JsonNode inv : flatArray) {
                    String ctin = firstText(inv, "supplierGstin", "ctin", "gstin").toUpperCase(Locale.ROOT);
                    String name = firstText(inv, "supplierName", "trdnm", "vendorName");
                    entities.add(mapGstr2bInvoice(notice, ctin, name, inv));
                }
            }

            gstr2bRepository.saveAll(entities);
            return entities.size();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid GSTR-2B JSON payload: " + ex.getMessage(), ex);
        }
    }

    @Transactional
    public int parseAndSavePurchaseRegister(GstNotice notice, byte[] purchaseJsonBytes) {
        if (purchaseJsonBytes == null || purchaseJsonBytes.length == 0) {
            return 0;
        }
        try {
            JsonNode root = objectMapper.readTree(purchaseJsonBytes);
            JsonNode arrayNode = root.isArray() ? root : root.path("invoices");
            if (!arrayNode.isArray()) {
                arrayNode = root.path("purchases");
            }

            List<PurchaseRegisterInvoice> entities = new ArrayList<>();
            if (arrayNode.isArray()) {
                for (JsonNode row : arrayNode) {
                    PurchaseRegisterInvoice inv = new PurchaseRegisterInvoice();
                    inv.setNotice(notice);
                    inv.setSupplierGstin(firstText(row, "supplierGstin", "ctin", "gstin").toUpperCase(Locale.ROOT));
                    inv.setSupplierName(firstText(row, "supplierName", "vendorName", "trdnm"));
                    inv.setInvoiceNumber(firstText(row, "invoiceNumber", "inum", "invNo").trim());
                    inv.setInvoiceDate(firstText(row, "invoiceDate", "dt", "invDate"));

                    BigDecimal txval = firstDecimal(row, "taxableValue", "txval");
                    BigDecimal igst = firstDecimal(row, "igst", "iamt");
                    BigDecimal cgst = firstDecimal(row, "cgst", "camt");
                    BigDecimal sgst = firstDecimal(row, "sgst", "samt");
                    BigDecimal totalItc = row.has("totalItc")
                            ? firstDecimal(row, "totalItc")
                            : igst.add(cgst).add(sgst);

                    inv.setTaxableValue(txval);
                    inv.setIgst(igst);
                    inv.setCgst(cgst);
                    inv.setSgst(sgst);
                    inv.setTotalItc(totalItc.setScale(2, RoundingMode.HALF_UP));
                    entities.add(inv);
                }
            }

            purchaseRepository.saveAll(entities);
            return entities.size();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid Purchase Register JSON payload: " + ex.getMessage(), ex);
        }
    }

    private Gstr2bInvoice mapGstr2bInvoice(GstNotice notice, String supplierGstin, String supplierName, JsonNode inv) {
        Gstr2bInvoice entity = new Gstr2bInvoice();
        entity.setNotice(notice);
        entity.setSupplierGstin(supplierGstin);
        entity.setSupplierName(supplierName);
        entity.setInvoiceNumber(firstText(inv, "inum", "invoiceNumber", "invNo").trim());
        entity.setInvoiceDate(firstText(inv, "dt", "invoiceDate", "invDate"));

        BigDecimal txval = firstDecimal(inv, "txval", "taxableValue");
        BigDecimal igst = firstDecimal(inv, "igst", "iamt");
        BigDecimal cgst = firstDecimal(inv, "cgst", "camt");
        BigDecimal sgst = firstDecimal(inv, "sgst", "samt");
        BigDecimal totalItc = inv.has("totalItc")
                ? firstDecimal(inv, "totalItc")
                : igst.add(cgst).add(sgst);

        entity.setTaxableValue(txval);
        entity.setIgst(igst);
        entity.setCgst(cgst);
        entity.setSgst(sgst);
        entity.setTotalItc(totalItc.setScale(2, RoundingMode.HALF_UP));
        entity.setItcAvailability(firstText(inv, "itcavl", "itcAvailability").isBlank()
                ? "Y" : firstText(inv, "itcavl", "itcAvailability"));
        return entity;
    }

    private String textOrEmpty(JsonNode node, String field) {
        JsonNode val = node.path(field);
        return val.isMissingNode() || val.isNull() ? "" : val.asText("");
    }

    private String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull()) {
                return node.get(key).asText("");
            }
        }
        return "";
    }

    private BigDecimal firstDecimal(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull()) {
                try {
                    return new BigDecimal(node.get(key).asText("0")).setScale(2, RoundingMode.HALF_UP);
                } catch (NumberFormatException ignored) {
                    return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
