package com.gstcopilot.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "draft_replies")
public class DraftReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notice_id", nullable = false)
    private GstNotice notice;

    @Column(name = "draft_text", nullable = false, columnDefinition = "LONGTEXT")
    private String draftText;

    @Column(name = "verified_citations_json", nullable = false, columnDefinition = "JSON")
    private String verifiedCitationsJson;

    @Column(name = "masked_tokens_json", nullable = false, columnDefinition = "JSON")
    private String maskedTokensJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public GstNotice getNotice() {
        return notice;
    }

    public void setNotice(GstNotice notice) {
        this.notice = notice;
    }

    public String getDraftText() {
        return draftText;
    }

    public void setDraftText(String draftText) {
        this.draftText = draftText;
    }

    public String getVerifiedCitationsJson() {
        return verifiedCitationsJson;
    }

    public void setVerifiedCitationsJson(String verifiedCitationsJson) {
        this.verifiedCitationsJson = verifiedCitationsJson;
    }

    public String getMaskedTokensJson() {
        return maskedTokensJson;
    }

    public void setMaskedTokensJson(String maskedTokensJson) {
        this.maskedTokensJson = maskedTokensJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
