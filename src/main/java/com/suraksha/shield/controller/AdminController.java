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
import org.springframework.data.domain.Page;

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

    /**
     * Kaggle CSV Import Endpoint
     *
     * Accepts a semicolon-delimited Kaggle insurance CSV and converts it
     * automatically to the project's Policy format.
     *
     * Supported Kaggle columns (auto-detected from header row):
     *   POLICY TYPE 1 → type
     *   Premium       → premium
     *   BENEFIT or INITIAL BENEFIT → coverage
     *   SUBSTANDARD RISK           → riskLevel (Yes=high, No=low)
     *   CHANNEL1                   → provider
     *   (name is auto-generated as "{provider} {type} Policy #{row}")
     */
    @PostMapping("/policies/import-kaggle")
    public ResponseEntity<?> importPoliciesFromKaggleCsv(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("message", "Please upload a non-empty CSV file.");
            return ResponseEntity.badRequest().body(response);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            response.put("message", "Only CSV files are accepted.");
            return ResponseEntity.badRequest().body(response);
        }

        Admin admin = getAuthenticatedAdmin();
        List<Policy> policiesToSave = new ArrayList<>();
        List<String> skippedRows = new ArrayList<>();
        int rowNumber = 0;

        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream())
            );

            // --- Read and parse the header row ---
            String headerLine = reader.readLine();
            if (headerLine == null) {
                response.put("message", "CSV file is empty or missing header row.");
                return ResponseEntity.badRequest().body(response);
            }

            // Kaggle files use semicolons as delimiters
            String[] headers = headerLine.split(";");
            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                colIndex.put(headers[i].trim().toUpperCase(), i);
            }

            // Validate required columns exist
            String[] requiredCols = {"POLICY TYPE 1", "PREMIUM"};
            for (String col : requiredCols) {
                if (!colIndex.containsKey(col)) {
                    response.put("message", "Missing required column: \"" + col + "\". Found headers: " + headerLine);
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Determine which coverage column to use
            String coverageCol = colIndex.containsKey("BENEFIT") ? "BENEFIT" : "INITIAL BENEFIT";

            // --- Read data rows ---
            String line;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(";", -1);

                try {
                    // Extract values using detected column indices
                    String rawType     = getCol(cols, colIndex, "POLICY TYPE 1");
                    String rawPremium  = getCol(cols, colIndex, "PREMIUM");
                    String rawCoverage = getCol(cols, colIndex, coverageCol);
                    String rawRisk     = getCol(cols, colIndex, "SUBSTANDARD RISK");
                    String rawProvider = getCol(cols, colIndex, "CHANNEL1");

                    // Clean and convert each field
                    String  type      = mapKaggleType(rawType);
                    Integer premium   = parseIntSafe(rawPremium);
                    Integer coverage  = parseIntSafe(rawCoverage);
                    String  riskLevel = mapKaggleRisk(rawRisk);
                    String  provider  = rawProvider.isEmpty() ? "Unknown" : rawProvider;

                    // Skip rows with zero/negative values (bad data)
                    if (premium <= 0 || coverage <= 0) {
                        skippedRows.add("Row " + rowNumber + ": invalid premium/coverage — \"" + line + "\"");
                        continue;
                    }

                    // Auto-generate a meaningful policy name
                    String name = provider + " " + capitalize(type) + " Policy #" + rowNumber;

                    policiesToSave.add(new Policy(name, type, premium, coverage, riskLevel, provider, admin));

                } catch (Exception e) {
                    skippedRows.add("Row " + rowNumber + ": parse error — " + e.getMessage());
                }
            }

            reader.close();

        } catch (Exception e) {
            response.put("message", "Error reading file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        policyRepository.saveAll(policiesToSave);

        response.put("message", "Kaggle import complete! " + policiesToSave.size() + " policies imported.");
        response.put("imported", policiesToSave.size());
        response.put("skipped", skippedRows.size());
        response.put("skippedDetails", skippedRows);
        return ResponseEntity.ok(response);
    }

    // ---- Kaggle Helper Methods ----

    /** Safely get a column value by name; returns empty string if column not found */
    private String getCol(String[] cols, Map<String, Integer> colIndex, String colName) {
        Integer idx = colIndex.get(colName.toUpperCase());
        if (idx == null || idx >= cols.length) return "";
        return cols[idx].trim();
    }

    /**
     * Maps Kaggle POLICY TYPE 1 values to project values:
     * life / health / car / home / travel
     */
    private String mapKaggleType(String raw) {
        String v = raw.toLowerCase().trim();
        if (v.contains("life"))                          return "life";
        if (v.contains("health") || v.contains("med"))  return "health";
        if (v.contains("motor") || v.contains("car") || v.contains("vehicle")) return "car";
        if (v.contains("home") || v.contains("house"))  return "home";
        if (v.contains("travel"))                        return "travel";
        return "life"; // default
    }

    /**
     * Maps Kaggle SUBSTANDARD RISK values to: low / medium / high
     * Common Kaggle values: Yes/No, 1/0, Low/Medium/High
     */
    private String mapKaggleRisk(String raw) {
        String v = raw.toLowerCase().trim();
        if (v.equals("yes") || v.equals("1") || v.equals("high")) return "high";
        if (v.equals("med") || v.equals("medium"))                 return "medium";
        return "low"; // No, 0, Low, or anything else → low
    }

    /** Parse a numeric string to Integer, removing ₹ $ and commas */
    private Integer parseIntSafe(String raw) {
        String cleaned = raw.replaceAll("[₹$,\\s]", "");
        if (cleaned.isEmpty()) return 0;
        return (int) Double.parseDouble(cleaned);
    }

    /** Capitalize first letter of a string */
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
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
