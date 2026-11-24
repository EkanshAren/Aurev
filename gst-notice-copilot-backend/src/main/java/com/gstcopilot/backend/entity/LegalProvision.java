package com.gstcopilot.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "legal_provisions")
public class LegalProvision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "citation_id", nullable = false, unique = true, length = 64)
    private String citationId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "section_ref", nullable = false, length = 128)
    private String sectionRef;

    @Column(name = "provision_type", nullable = false, length = 64)
    private String provisionType;

    @Column(name = "applicable_forms", nullable = false, length = 128)
    private String applicableForms;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "full_text", nullable = false, columnDefinition = "LONGTEXT")
    private String fullText;

    @Column(name = "embedding_json", columnDefinition = "JSON")
    private String embeddingJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public String getCitationId() {
        return citationId;
    }

    public void setCitationId(String citationId) {
        this.citationId = citationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSectionRef() {
        return sectionRef;
    }

    public void setSectionRef(String sectionRef) {
        this.sectionRef = sectionRef;
    }

    public String getProvisionType() {
        return provisionType;
    }

    public void setProvisionType(String provisionType) {
        this.provisionType = provisionType;
    }

    public String getApplicableForms() {
        return applicableForms;
    }

    public void setApplicableForms(String applicableForms) {
        this.applicableForms = applicableForms;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getEmbeddingJson() {
        return embeddingJson;
    }

    public void setEmbeddingJson(String embeddingJson) {
        this.embeddingJson = embeddingJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
