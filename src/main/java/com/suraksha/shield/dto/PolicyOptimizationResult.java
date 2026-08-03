package com.suraksha.shield.dto;

import com.suraksha.shield.entity.Policy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyOptimizationResult {
    private List<Policy> policies;
    private boolean combinationMode;
    private int totalPremium;
    private int totalCoverage;
}
