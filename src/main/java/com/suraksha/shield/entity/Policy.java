package com.suraksha.shield.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String type;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer premium;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer coverage;

    @NotBlank
    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @NotBlank
    @Column(nullable = false)
    private String provider;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id")
    private Admin createdBy;

    // Constructors
    public Policy() {
    }

    public Policy(String name, String type, Integer premium, Integer coverage, String riskLevel, String provider, Admin createdBy) {
        this.name = name;
        this.type = type;
        this.premium = premium;
        this.coverage = coverage;
        this.riskLevel = riskLevel;
        this.provider = provider;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPremium() {
        return premium;
    }

    public void setPremium(Integer premium) {
        this.premium = premium;
    }

    public Integer getCoverage() {
        return coverage;
    }

    public void setCoverage(Integer coverage) {
        this.coverage = coverage;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Admin getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Admin createdBy) {
        this.createdBy = createdBy;
    }
}
