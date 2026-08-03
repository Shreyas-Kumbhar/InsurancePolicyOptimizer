package com.suraksha.shield.config;

import com.suraksha.shield.entity.Admin;
import com.suraksha.shield.entity.Policy;
import com.suraksha.shield.repository.AdminRepository;
import com.suraksha.shield.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {


        // 1. Seed Admin User
        Admin admin = null;
        if (adminRepository.count() == 0) {
            admin = new Admin();
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin = adminRepository.save(admin);
            System.out.println(">>> Seeded Admin account: admin@gmail.com (password: admin123)");
        } else {
            admin = adminRepository.findAll().get(0);
        }

        // 2. Seed Policies from CSV file (only if no policies exist yet)
        if (policyRepository.count() == 0) {
            List<Policy> policies = loadPoliciesFromCsv(admin);
            policyRepository.saveAll(policies);
            System.out.println(">>> Seeded " + policies.size() + " policies from policies.csv");
        }
    }

    /**
     * Reads policies from src/main/resources/policies.csv
     *
     * Expected CSV format (first row is the header, which is skipped):
     *   name,type,premium,coverage,riskLevel,provider
     *
     * Example row:
     *   LIC Jeevan Labh,life,6500,500000,low,LIC
     */
    private List<Policy> loadPoliciesFromCsv(Admin admin) {
        List<Policy> policies = new ArrayList<>();

        try {
            // ClassPathResource looks inside src/main/resources automatically
            ClassPathResource resource = new ClassPathResource("policies.csv");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream())
            );

            String line;
            boolean isFirstLine = true; // Used to skip the header row

            while ((line = reader.readLine()) != null) {

                // Skip the header row: name,type,premium,coverage,riskLevel,provider
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Split each line by comma to get individual column values
                String[] columns = line.split(",");

                // Make sure the row has all 6 expected columns
                if (columns.length < 6) {
                    System.out.println(">>> Skipping invalid CSV row: " + line);
                    continue;
                }

                // Map each column to the correct Policy field
                String name      = columns[0].trim(); // e.g. "LIC Jeevan Labh"
                String type      = columns[1].trim(); // e.g. "life"
                Integer premium  = Integer.parseInt(columns[2].trim()); // e.g. 6500
                Integer coverage = Integer.parseInt(columns[3].trim()); // e.g. 500000
                String riskLevel = columns[4].trim(); // e.g. "low"
                String provider  = columns[5].trim(); // e.g. "LIC"

                policies.add(Policy.builder()
                        .name(name)
                        .type(type)
                        .premium(premium)
                        .coverage(coverage)
                        .riskLevel(riskLevel)
                        .provider(provider)
                        .createdBy(admin)
                        .build());
            }

            reader.close();

        } catch (Exception e) {
            System.out.println(">>> ERROR reading policies.csv: " + e.getMessage());
            e.printStackTrace();
        }

        return policies;
    }
}
