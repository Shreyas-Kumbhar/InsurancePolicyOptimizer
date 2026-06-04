package com.suraksha.shield.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PolicyDto {

    private Long id;

    @NotBlank(message = "Policy name is required")
    private String name;

    @NotBlank(message = "Policy type is required")
    private String type;

    @NotNull(message = "Premium is required")
    @Min(value = 0, message = "Premium must be non-negative")
    private Integer premium;

    @NotNull(message = "Coverage is required")
    @Min(value = 0, message = "Coverage must be non-negative")
    private Integer coverage;

    @NotBlank(message = "Risk level is required")
    private String riskLevel;

    @NotBlank(message = "Provider is required")
    private String provider;

    private Long createdById;

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

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }
}
