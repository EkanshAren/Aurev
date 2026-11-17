package com.aurev.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ca_firm_id", nullable = false)
    private CaFirm caFirm;

    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;

    @Column(nullable = false, unique = true, length = 15)
    private String gstin;

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CaFirm getCaFirm() { return caFirm; }
    public void setCaFirm(CaFirm caFirm) { this.caFirm = caFirm; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    public String getStateCode() { return stateCode; }
    public void setStateCode(String stateCode) { this.stateCode = stateCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
