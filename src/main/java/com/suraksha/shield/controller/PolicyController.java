package com.suraksha.shield.controller;

import com.suraksha.shield.dto.PolicyOptimizationResult;
import com.suraksha.shield.entity.Policy;
import com.suraksha.shield.entity.User;
import com.suraksha.shield.repository.UserRepository;
import com.suraksha.shield.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getPolicyDetail(@PathVariable Long id) {
        Policy policy = policyService.getPolicyById(id);
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/results")
    public ResponseEntity<?> getOptimizationResults(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(name = "riskLevel", required = false) String riskLevel,
            @RequestParam(name = "max_premium", required = false) Integer maxPremium,
            @RequestParam(name = "coverage_min", required = false) Integer coverageMin,
            @RequestParam(name = "coverage_max", required = false) Integer coverageMax) {

        PolicyOptimizationResult result = policyService.optimize(type, riskLevel, name, maxPremium, coverageMin, coverageMax);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/allocate")
    public ResponseEntity<?> allocatePolicy(@PathVariable Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Policy policy = policyService.getPolicyById(id);

        Map<String, String> response = new HashMap<>();
        
        // Prevent duplicate allocation
        boolean alreadyAllocated = user.getAllocatedPolicies().stream()
                .anyMatch(p -> p.getId().equals(policy.getId()));

        if (alreadyAllocated) {
            response.put("message", "You have already allocated this policy.");
            return ResponseEntity.badRequest().body(response);
        }

        user.getAllocatedPolicies().add(policy);
        userRepository.save(user);

        response.put("message", "Policy successfully allocated to your profile!");
        return ResponseEntity.ok(response);
    }
}
