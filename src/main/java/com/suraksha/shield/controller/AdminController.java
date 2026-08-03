package com.suraksha.shield.controller;

import com.suraksha.shield.dto.PolicyDto;
import com.suraksha.shield.entity.Admin;
import com.suraksha.shield.entity.Policy;
import com.suraksha.shield.repository.AdminRepository;
import com.suraksha.shield.service.CsvImportService;
import com.suraksha.shield.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PolicyService policyService;
    private final AdminRepository adminRepository;
    private final CsvImportService csvImportService;

    @GetMapping("/dashboard")
    public ResponseEntity<Page<Policy>> getAdminDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<Policy> policies = policyService.getPaginatedPolicies(page, size);
        return ResponseEntity.ok(policies);
    }

    @PostMapping("/policies")
    public ResponseEntity<?> createPolicy(@Valid @RequestBody PolicyDto policyDto) {
        Admin admin = getAuthenticatedAdmin();
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

    @PostMapping("/policies/import")
    public ResponseEntity<?> importPoliciesFromCsv(@RequestParam("file") MultipartFile file) {
        Admin admin = getAuthenticatedAdmin();
        Map<String, Object> response = csvImportService.importCsv(file, admin);
        return ResponseEntity.ok(response);
    }

    // Helper: get the currently logged-in admin from the security context
    private Admin getAuthenticatedAdmin() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails)
            ? ((UserDetails) principal).getUsername()
            : principal.toString();
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }
}
