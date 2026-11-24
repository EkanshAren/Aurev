package com.gstcopilot.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "gst_notices")
public class GstNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "din", nullable = false, length = 32)
    private String din;

    @Column(name = "taxpayer_gstin", nullable = false, length = 15)
    private String taxpayerGstin;

    @Column(name = "taxpayer_name", length = 255)
    private String taxpayerName;

    @Column(name = "form_type", nullable = false, length = 32)
    private String formType;

    @Column(name = "tax_period", length = 64)
    private String taxPeriod;

    @Column(name = "due_date", length = 32)
    private String dueDate;

    @Column(name = "demanded_itc", nullable = false, precision = 15, scale = 2)
    private BigDecimal demandedItc = BigDecimal.ZERO;

    @Column(name = "raw_notice_text", columnDefinition = "LONGTEXT")
    private String rawNoticeText;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "UPLOADED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDin() {
        return din;
    }

    public void setDin(String din) {
        this.din = din;
    }

    public String getTaxpayerGstin() {
        return taxpayerGstin;
    }

    public void setTaxpayerGstin(String taxpayerGstin) {
        this.taxpayerGstin = taxpayerGstin;
    }

    public String getTaxpayerName() {
        return taxpayerName;
    }

    public void setTaxpayerName(String taxpayerName) {
        this.taxpayerName = taxpayerName;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public String getTaxPeriod() {
        return taxPeriod;
    }

    public void setTaxPeriod(String taxPeriod) {
        this.taxPeriod = taxPeriod;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getDemandedItc() {
        return demandedItc;
    }

    public void setDemandedItc(BigDecimal demandedItc) {
        this.demandedItc = demandedItc != null ? demandedItc : BigDecimal.ZERO;
    }

    public String getRawNoticeText() {
        return rawNoticeText;
    }

    public void setRawNoticeText(String rawNoticeText) {
        this.rawNoticeText = rawNoticeText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
