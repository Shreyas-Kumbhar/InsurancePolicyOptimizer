package com.suraksha.shield.controller;

import com.suraksha.shield.dto.PolicyDto;
import com.suraksha.shield.entity.Admin;
import com.suraksha.shield.entity.Policy;
import com.suraksha.shield.repository.AdminRepository;
import com.suraksha.shield.repository.PolicyRepository;
import com.suraksha.shield.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

    @Autowired
    private PolicyRepository policyRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<List<Policy>> getAdminDashboard() {
        List<Policy> policies = policyService.getAllPolicies();
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

    /**
     * CSV Import Endpoint
     *
     * Accepts a multipart CSV file upload and saves all valid rows as policies.
     *
     * Expected CSV format (first row must be the header):
     *   name,type,premium,coverage,riskLevel,provider
     *
     * Example:
     *   LIC Jeevan Labh,life,6500,500000,low,LIC
     */
    @PostMapping("/policies/import")
    public ResponseEntity<?> importPoliciesFromCsv(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        // Validate file is not empty
        if (file.isEmpty()) {
            response.put("message", "Please upload a non-empty CSV file.");
            return ResponseEntity.badRequest().body(response);
        }

        // Validate it is a CSV file
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            response.put("message", "Only CSV files are accepted.");
            return ResponseEntity.badRequest().body(response);
        }

        Admin admin = getAuthenticatedAdmin();

        List<Policy> policiesToSave = new ArrayList<>();
        List<String> skippedRows = new ArrayList<>();
        int lineNumber = 0;

        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream())
            );

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip the header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Skip blank lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = line.split(",");

                // Must have exactly 6 columns
                if (columns.length < 6) {
                    skippedRows.add("Row " + lineNumber + ": not enough columns — \"" + line + "\"");
                    continue;
                }

                try {
                    String name      = columns[0].trim();
                    String type      = columns[1].trim().toLowerCase();
                    Integer premium  = Integer.parseInt(columns[2].trim());
                    Integer coverage = Integer.parseInt(columns[3].trim());
                    String riskLevel = columns[4].trim().toLowerCase();
                    String provider  = columns[5].trim();

                    // Basic validation
                    if (name.isEmpty() || type.isEmpty() || provider.isEmpty()) {
                        skippedRows.add("Row " + lineNumber + ": name, type, or provider is blank — \"" + line + "\"");
                        continue;
                    }

                    policiesToSave.add(new Policy(name, type, premium, coverage, riskLevel, provider, admin));

                } catch (NumberFormatException e) {
                    skippedRows.add("Row " + lineNumber + ": premium/coverage must be a number — \"" + line + "\"");
                }
            }

            reader.close();

        } catch (Exception e) {
            response.put("message", "Error reading CSV file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        // Save all valid policies in one batch
        policyRepository.saveAll(policiesToSave);

        response.put("message", "Import complete! " + policiesToSave.size() + " policies imported successfully.");
        response.put("imported", policiesToSave.size());
        response.put("skipped", skippedRows.size());
        response.put("skippedDetails", skippedRows);
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
