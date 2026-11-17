package com.aurev.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ca_firms")
public class CaFirm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firm_name", nullable = false, length = 150)
    private String firmName;

    @Column(name = "icai_reg_no", nullable = false, unique = true, length = 30)
    private String icaiRegNo;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirmName() { return firmName; }
    public void setFirmName(String firmName) { this.firmName = firmName; }
    public String getIcaiRegNo() { return icaiRegNo; }
    public void setIcaiRegNo(String icaiRegNo) { this.icaiRegNo = icaiRegNo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
