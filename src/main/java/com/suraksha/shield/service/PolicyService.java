package com.suraksha.shield.service;

import com.suraksha.shield.dto.PolicyDto;
import com.suraksha.shield.dto.PolicyOptimizationResult;
import com.suraksha.shield.entity.Admin;
import com.suraksha.shield.entity.Policy;
import com.suraksha.shield.exception.ResourceNotFoundException;
import com.suraksha.shield.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;

    public List<Policy> getAllPolicies() {
        return policyRepository.findTop100Policies();
    }

    public Page<Policy> getPaginatedPolicies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return policyRepository.findAll(pageable);
    }

    public Policy getPolicyById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
    }

    public Policy createPolicy(PolicyDto policyDto, Admin admin) {
        Policy policy = new Policy();
        policy.setName(policyDto.getName());
        policy.setType(policyDto.getType());
        policy.setPremium(policyDto.getPremium());
        policy.setCoverage(policyDto.getCoverage());
        policy.setRiskLevel(policyDto.getRiskLevel());
        policy.setProvider(policyDto.getProvider());
        policy.setCreatedBy(admin);
        return policyRepository.save(policy);
    }

    public Policy updatePolicy(Long id, PolicyDto policyDto) {
        Policy policy = getPolicyById(id);
        policy.setName(policyDto.getName());
        policy.setType(policyDto.getType());
        policy.setPremium(policyDto.getPremium());
        policy.setCoverage(policyDto.getCoverage());
        policy.setRiskLevel(policyDto.getRiskLevel());
        policy.setProvider(policyDto.getProvider());
        return policyRepository.save(policy);
    }

    public void deletePolicy(Long id) {
        Policy policy = getPolicyById(id);
        policyRepository.delete(policy);
    }

    // Optimization Engine implementing iterative subset search with pruning
    public PolicyOptimizationResult optimize(String type, String riskLevel, String name,
                                             Integer maxPremium, Integer coverageMin, Integer coverageMax) {
        if (maxPremium == null) maxPremium = 20000;
        if (coverageMin == null) coverageMin = 0;
        if (coverageMax == null) coverageMax = 5000000;

        if (name != null && !name.trim().isEmpty()) {
            List<Policy> matching = policyRepository.findByNameContainingIgnoreCase(name);
            matching.sort(Comparator.comparingInt(Policy::getPremium));
            return new PolicyOptimizationResult(matching, false, 0, 0);
        }

        List<Policy> filteredPolicies;
        boolean hasType = (type != null && !type.equalsIgnoreCase("All Types") && !type.trim().isEmpty());
        boolean hasRisk = (riskLevel != null && !riskLevel.equalsIgnoreCase("All Levels")
                && !riskLevel.equalsIgnoreCase("Any Risk Level") && !riskLevel.trim().isEmpty());

        if (hasType && hasRisk) {
            filteredPolicies = policyRepository.findTop200ByTypeAndRiskLevel(type, riskLevel, maxPremium);
        } else if (hasType) {
            filteredPolicies = policyRepository.findTop200ByType(type, maxPremium);
        } else if (hasRisk) {
            filteredPolicies = policyRepository.findTop200ByRiskLevel(riskLevel, maxPremium);
        } else {
            filteredPolicies = policyRepository.findTop200MostEfficient(maxPremium);
        }

        java.util.Map<Integer, Policy> bestByPremium = new java.util.HashMap<>();
        for (Policy p : filteredPolicies) {
            int prm = p.getPremium();
            if (!bestByPremium.containsKey(prm) || bestByPremium.get(prm).getCoverage() < p.getCoverage()) {
                bestByPremium.put(prm, p);
            }
        }
        
        List<Policy> uniquePolicies = new ArrayList<>(bestByPremium.values());
        uniquePolicies.sort((p1, p2) -> {
            double eff1 = (double) p1.getCoverage() / p1.getPremium();
            double eff2 = (double) p2.getCoverage() / p2.getPremium();
            return Double.compare(eff2, eff1);
        });

        int[] dp = new int[maxPremium + 1];
        java.util.Arrays.fill(dp, -1);
        dp[0] = 0;
        
        List<Policy>[] dpCombos = new ArrayList[maxPremium + 1];
        dpCombos[0] = new ArrayList<>();

        for (Policy p : uniquePolicies) {
            int cost = p.getPremium();
            int val = p.getCoverage();
            
            for (int w = maxPremium; w >= cost; w--) {
                if (dp[w - cost] != -1) {
                    int newCoverage = dp[w - cost] + val;
                    if (newCoverage <= coverageMax) {
                        if (newCoverage > dp[w]) {
                            dp[w] = newCoverage;
                            List<Policy> newCombo = new ArrayList<>(dpCombos[w - cost]);
                            newCombo.add(p);
                            dpCombos[w] = newCombo;
                        }
                    }
                }
            }
        }

        int bestPrm = -1;
        int maxCov = -1;

        for (int w = 0; w <= maxPremium; w++) {
            if (dp[w] >= coverageMin && dp[w] <= coverageMax) {
                if (dp[w] > maxCov) {
                    maxCov = dp[w];
                    bestPrm = w;
                }
            }
        }

        if (bestPrm != -1) {
            List<Policy> bestCombination = dpCombos[bestPrm];
            bestCombination.sort(Comparator.comparingInt(Policy::getPremium));
            return new PolicyOptimizationResult(bestCombination, true, bestPrm, dp[bestPrm]);
        }
        
        return new PolicyOptimizationResult(new ArrayList<>(), true, 0, 0);
    }
}
