package com.aurev.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gst_notices")
public class GstNotice {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "notice_type", nullable = false, length = 30)
    private String noticeType;

    @Column(name = "din_number", unique = true, length = 40)
    private String dinNumber;

    @Column(name = "tax_period", nullable = false, length = 20)
    private String taxPeriod;

    @Column(name = "section_invoked", nullable = false, length = 60)
    private String sectionInvoked;

    @Column(name = "demand_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal demandAmount = BigDecimal.ZERO;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "raw_extracted_text", columnDefinition = "TEXT")
    private String rawExtractedText;

    @Column(name = "masked_text", columnDefinition = "TEXT")
    private String maskedText;

    @Column(nullable = false, length = 30)
    private String status = "UPLOADED";

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public String getNoticeType() { return noticeType; }
    public void setNoticeType(String noticeType) { this.noticeType = noticeType; }
    public String getDinNumber() { return dinNumber; }
    public void setDinNumber(String dinNumber) { this.dinNumber = dinNumber; }
    public String getTaxPeriod() { return taxPeriod; }
    public void setTaxPeriod(String taxPeriod) { this.taxPeriod = taxPeriod; }
    public String getSectionInvoked() { return sectionInvoked; }
    public void setSectionInvoked(String sectionInvoked) { this.sectionInvoked = sectionInvoked; }
    public BigDecimal getDemandAmount() { return demandAmount; }
    public void setDemandAmount(BigDecimal demandAmount) { this.demandAmount = demandAmount; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getRawExtractedText() { return rawExtractedText; }
    public void setRawExtractedText(String rawExtractedText) { this.rawExtractedText = rawExtractedText; }
    public String getMaskedText() { return maskedText; }
    public void setMaskedText(String maskedText) { this.maskedText = maskedText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
