package com.gstcopilot.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_register_invoices")
public class PurchaseRegisterInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notice_id", nullable = false)
    private GstNotice notice;

    @Column(name = "supplier_gstin", nullable = false, length = 15)
    private String supplierGstin;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Column(name = "invoice_number", nullable = false, length = 64)
    private String invoiceNumber;

    @Column(name = "invoice_date", length = 32)
    private String invoiceDate;

    @Column(name = "taxable_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxableValue = BigDecimal.ZERO;

    @Column(name = "igst", nullable = false, precision = 15, scale = 2)
    private BigDecimal igst = BigDecimal.ZERO;

    @Column(name = "cgst", nullable = false, precision = 15, scale = 2)
    private BigDecimal cgst = BigDecimal.ZERO;

    @Column(name = "sgst", nullable = false, precision = 15, scale = 2)
    private BigDecimal sgst = BigDecimal.ZERO;

    @Column(name = "total_itc", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalItc = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public GstNotice getNotice() {
        return notice;
    }

    public void setNotice(GstNotice notice) {
        this.notice = notice;
    }

    public String getSupplierGstin() {
        return supplierGstin;
    }

    public void setSupplierGstin(String supplierGstin) {
        this.supplierGstin = supplierGstin;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public BigDecimal getTaxableValue() {
        return taxableValue;
    }

    public void setTaxableValue(BigDecimal taxableValue) {
        this.taxableValue = taxableValue != null ? taxableValue : BigDecimal.ZERO;
    }

    public BigDecimal getIgst() {
        return igst;
    }

    public void setIgst(BigDecimal igst) {
        this.igst = igst != null ? igst : BigDecimal.ZERO;
    }

    public BigDecimal getCgst() {
        return cgst;
    }

    public void setCgst(BigDecimal cgst) {
        this.cgst = cgst != null ? cgst : BigDecimal.ZERO;
    }

    public BigDecimal getSgst() {
        return sgst;
    }

    public void setSgst(BigDecimal sgst) {
        this.sgst = sgst != null ? sgst : BigDecimal.ZERO;
    }

    public BigDecimal getTotalItc() {
        return totalItc;
    }

    public void setTotalItc(BigDecimal totalItc) {
        this.totalItc = totalItc != null ? totalItc : BigDecimal.ZERO;
    }
}
