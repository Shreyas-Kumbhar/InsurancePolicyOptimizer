package com.suraksha.shield.service;

import com.suraksha.shield.dto.PolicyDto;
import com.suraksha.shield.dto.PolicyOptimizationResult;
import com.suraksha.shield.entity.Admin;
import com.suraksha.shield.entity.Policy;
import com.suraksha.shield.exception.ResourceNotFoundException;
import com.suraksha.shield.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PolicyService {

    @Autowired
    private PolicyRepository policyRepository;

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

        // Handle defaults
        if (maxPremium == null) maxPremium = 20000;
        if (coverageMin == null) coverageMin = 0;
        if (coverageMax == null) coverageMax = 5000000;

        // Search by name bypasses optimization
        if (name != null && !name.trim().isEmpty()) {
            List<Policy> matching = policyRepository.findByNameContainingIgnoreCase(name);
            matching.sort(Comparator.comparingInt(Policy::getPremium));
            return new PolicyOptimizationResult(matching, false, 0, 0);
        }

        // Apply filters
        List<Policy> filteredPolicies;
        boolean hasType = (type != null && !type.equalsIgnoreCase("All Types") && !type.trim().isEmpty());
        boolean hasRisk = (riskLevel != null && !riskLevel.equalsIgnoreCase("All Levels")
                && !riskLevel.equalsIgnoreCase("Any Risk Level") && !riskLevel.trim().isEmpty());

        if (hasType && hasRisk) {
            filteredPolicies = policyRepository.findByTypeIgnoreCaseAndRiskLevelIgnoreCase(type, riskLevel);
        } else if (hasType) {
            filteredPolicies = policyRepository.findByTypeIgnoreCase(type);
        } else if (hasRisk) {
            filteredPolicies = policyRepository.findByRiskLevelIgnoreCase(riskLevel);
        } else {
            filteredPolicies = policyRepository.findAll();
        }

        // Sort by premium ascending — helps pruning find good solutions early
        filteredPolicies.sort(Comparator.comparingInt(Policy::getPremium));

        // Cap at 20 policies to keep 2^n tractable (2^20 = ~1M combinations, safe)
        if (filteredPolicies.size() > 20) {
            filteredPolicies = filteredPolicies.subList(0, 20);
        }

        // Run backtracking
        List<Policy> bestCombination = new ArrayList<>();
        int[] minPremium = new int[]{Integer.MAX_VALUE};

        backtrack(filteredPolicies, 0, new ArrayList<>(), 0, 0,
                coverageMin, coverageMax, maxPremium, bestCombination, minPremium);

        bestCombination.sort(Comparator.comparingInt(Policy::getPremium));

        int totalCoverage = bestCombination.stream().mapToInt(Policy::getCoverage).sum();
        int totalPremium = minPremium[0] == Integer.MAX_VALUE ? 0 : minPremium[0];

        return new PolicyOptimizationResult(bestCombination, true, totalPremium, totalCoverage);
    }

    private void backtrack(List<Policy> allPolicies, int index, List<Policy> currentCombination,
                           int currentPremium, int currentCoverage, int coverageMin, int coverageMax,
                           int maxPremium, List<Policy> bestCombination, int[] minPremium) {

        // Valid combination found — record if it's the cheapest so far
        if (currentCoverage >= coverageMin && currentPremium <= maxPremium) {
            if (currentPremium < minPremium[0]) {
                minPremium[0] = currentPremium;
                bestCombination.clear();
                bestCombination.addAll(currentCombination);
            }
        }

        // Base case: exhausted all policies or exceeded budget
        if (index >= allPolicies.size() || currentPremium >= maxPremium) {
            return;
        }

        // Pruning: if even adding ALL remaining policies can't reach coverageMin, skip branch
        int remainingCoverage = 0;
        for (int i = index; i < allPolicies.size(); i++) {
            remainingCoverage += allPolicies.get(i).getCoverage();
        }
        if (currentCoverage + remainingCoverage < coverageMin) {
            return;
        }

        // Pruning: current path already more expensive than best found — abandon
        if (currentPremium >= minPremium[0]) {
            return;
        }

        Policy policy = allPolicies.get(index);

        // Choice 1: Include current policy (if within budget and coverage cap)
        if (currentPremium + policy.getPremium() <= maxPremium
                && currentCoverage + policy.getCoverage() <= coverageMax) {
            currentCombination.add(policy);
            backtrack(allPolicies, index + 1, currentCombination,
                    currentPremium + policy.getPremium(),
                    currentCoverage + policy.getCoverage(),
                    coverageMin, coverageMax, maxPremium, bestCombination, minPremium);
            currentCombination.remove(currentCombination.size() - 1);
        }

        // Choice 2: Exclude current policy
        backtrack(allPolicies, index + 1, currentCombination, currentPremium, currentCoverage,
                coverageMin, coverageMax, maxPremium, bestCombination, minPremium);
    }
}
