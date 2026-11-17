package com.aurev.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "legal_citations")
public class LegalCitation {

    @Id
    @Column(name = "citation_id", length = 64)
    private String citationId;

    @Column(name = "act_or_circular", nullable = false, length = 120)
    private String actOrCircular;

    @Column(name = "section_ref", nullable = false, length = 80)
    private String sectionRef;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    public String getCitationId() { return citationId; }
    public void setCitationId(String citationId) { this.citationId = citationId; }
    public String getActOrCircular() { return actOrCircular; }
    public void setActOrCircular(String actOrCircular) { this.actOrCircular = actOrCircular; }
    public String getSectionRef() { return sectionRef; }
    public void setSectionRef(String sectionRef) { this.sectionRef = sectionRef; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
}
