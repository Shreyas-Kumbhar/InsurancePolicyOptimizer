package com.suraksha.shield.dto;

import com.suraksha.shield.entity.Policy;
import java.util.List;

public class PolicyOptimizationResult {
    private List<Policy> policies;
    private boolean combinationMode;
    private int totalPremium;
    private int totalCoverage;

    // Constructors
    public PolicyOptimizationResult() {
    }

    public PolicyOptimizationResult(List<Policy> policies, boolean combinationMode, int totalPremium, int totalCoverage) {
        this.policies = policies;
        this.combinationMode = combinationMode;
        this.totalPremium = totalPremium;
        this.totalCoverage = totalCoverage;
    }

    // Getters and Setters
    public List<Policy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<Policy> policies) {
        this.policies = policies;
    }

    public boolean isCombinationMode() {
        return combinationMode;
    }

    public void setCombinationMode(boolean combinationMode) {
        this.combinationMode = combinationMode;
    }

    public int getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(int totalPremium) {
        this.totalPremium = totalPremium;
    }

    public int getTotalCoverage() {
        return totalCoverage;
    }

    public void setTotalCoverage(int totalCoverage) {
        this.totalCoverage = totalCoverage;
    }
}
