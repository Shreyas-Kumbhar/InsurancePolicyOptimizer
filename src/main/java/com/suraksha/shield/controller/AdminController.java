package com.suraksha.shield.controller;

import com.suraksha.shield.dto.PolicyDto;
import com.suraksha.shield.entity.Admin;
import com.suraksha.shield.entity.Policy;
import com.suraksha.shield.repository.AdminRepository;
import com.suraksha.shield.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private AdminRepository adminRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<List<Policy>> getAdminDashboard() {
        List<Policy> policies = policyService.getAllPolicies();
        return ResponseEntity.ok(policies);
    }

    @PostMapping("/policies")
    public ResponseEntity<?> createPolicy(@Valid @RequestBody PolicyDto policyDto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Policy created = policyService.createPolicy(policyDto, admin);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/policies/{id}")
    public ResponseEntity<?> updatePolicy(@PathVariable Long id, @Valid @RequestBody PolicyDto policyDto) {
        Policy updated = policyService.updatePolicy(id, policyDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/policies/{id}")
    public ResponseEntity<?> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Policy deleted successfully!");
        return ResponseEntity.ok(response);
    }
}
