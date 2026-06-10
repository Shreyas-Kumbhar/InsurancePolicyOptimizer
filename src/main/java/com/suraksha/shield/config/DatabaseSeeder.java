package com.suraksha.shield.config;

import com.suraksha.shield.entity.Admin;
import com.suraksha.shield.entity.Policy;
import com.suraksha.shield.repository.AdminRepository;
import com.suraksha.shield.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

        // 2. Seed Sample Policies
        if (policyRepository.count() == 0) {
            List<Policy> samplePolicies = new ArrayList<>();

            // LIFE POLICIES
            samplePolicies.add(new Policy("LIC Jeevan Suraksha 1", "life", 5000, 300000, "low", "LIC", admin));
            samplePolicies.add(new Policy("HDFC Life Shield 2", "life", 5200, 320000, "medium", "HDFC Life", admin));
            samplePolicies.add(new Policy("ICICI Life Secure 3", "life", 5400, 340000, "high", "ICICI Prudential", admin));
            samplePolicies.add(new Policy("Max Life Protect 4", "life", 5600, 360000, "low", "Max Life", admin));
            samplePolicies.add(new Policy("SBI Life Cover 5", "life", 5800, 380000, "medium", "SBI Life", admin));
            samplePolicies.add(new Policy("LIC Jeevan Anand 6", "life", 6000, 400000, "high", "LIC", admin));
            samplePolicies.add(new Policy("HDFC Smart Life 7", "life", 6200, 420000, "low", "HDFC Life", admin));
            samplePolicies.add(new Policy("ICICI Future Secure 8", "life", 6400, 440000, "medium", "ICICI Prudential", admin));
            samplePolicies.add(new Policy("Max Life Growth 9", "life", 6600, 460000, "high", "Max Life", admin));
            samplePolicies.add(new Policy("SBI Life Plus 10", "life", 6800, 480000, "low", "SBI Life", admin));

            // HEALTH POLICIES
            samplePolicies.add(new Policy("Star Health Suraksha 1", "health", 3000, 150000, "low", "Star Health", admin));
            samplePolicies.add(new Policy("Niva Bupa Care 2", "health", 3200, 170000, "medium", "Niva Bupa", admin));
            samplePolicies.add(new Policy("Care Health Shield 3", "health", 3400, 190000, "high", "Care Health", admin));
            samplePolicies.add(new Policy("Aditya Health Plan 4", "health", 3600, 210000, "low", "Aditya Birla Health", admin));
            samplePolicies.add(new Policy("Reliance Health Guard 5", "health", 3800, 230000, "medium", "Reliance Health", admin));
            samplePolicies.add(new Policy("Star Health Assure 6", "health", 4000, 250000, "high", "Star Health", admin));
            samplePolicies.add(new Policy("Niva Bupa Reassure 7", "health", 4200, 270000, "low", "Niva Bupa", admin));
            samplePolicies.add(new Policy("Care Health Secure 8", "health", 4400, 290000, "medium", "Care Health", admin));
            samplePolicies.add(new Policy("Aditya Health Plus 9", "health", 4600, 310000, "high", "Aditya Birla Health", admin));
            samplePolicies.add(new Policy("Reliance Health Elite 10", "health", 4800, 330000, "low", "Reliance Health", admin));

            // VEHICLE POLICIES (Car/Two-Wheeler)
            samplePolicies.add(new Policy("ICICI Lombard Motor 1", "car", 2500, 200000, "low", "ICICI Lombard", admin));
            samplePolicies.add(new Policy("HDFC ERGO Drive 2", "car", 2700, 220000, "medium", "HDFC ERGO", admin));
            samplePolicies.add(new Policy("Bajaj Drive Secure 3", "car", 2900, 240000, "high", "Bajaj Allianz", admin));
            samplePolicies.add(new Policy("Tata AIG Auto 4", "car", 3100, 260000, "low", "Tata AIG", admin));
            samplePolicies.add(new Policy("SBI Car Guard 5", "car", 3300, 280000, "medium", "SBI General", admin));
            samplePolicies.add(new Policy("ICICI Lombard Elite 6", "car", 3500, 300000, "high", "ICICI Lombard", admin));
            samplePolicies.add(new Policy("HDFC Drive Plus 7", "car", 3700, 320000, "low", "HDFC ERGO", admin));
            samplePolicies.add(new Policy("Bajaj Drive Pro 8", "car", 3900, 340000, "medium", "Bajaj Allianz", admin));
            samplePolicies.add(new Policy("Tata Auto Shield 9", "car", 4100, 360000, "high", "Tata AIG", admin));
            samplePolicies.add(new Policy("SBI Motor Secure 10", "car", 4300, 380000, "low", "SBI General", admin));

            // TRAVEL/HOME POLICIES
            samplePolicies.add(new Policy("HDFC ERGO Home Secure", "home", 1500, 500000, "low", "HDFC ERGO", admin));
            samplePolicies.add(new Policy("ICICI Lombard Home Shield", "home", 2000, 750000, "medium", "ICICI Lombard", admin));
            samplePolicies.add(new Policy("Bajaj Home Care", "home", 2500, 1000000, "high", "Bajaj Allianz", admin));
            samplePolicies.add(new Policy("SBI Travel Guard", "travel", 800, 100000, "low", "SBI General", admin));
            samplePolicies.add(new Policy("Tata AIG Globe Travel", "travel", 1200, 200000, "medium", "Tata AIG", admin));

            policyRepository.saveAll(samplePolicies);
            System.out.println(">>> Seeded " + samplePolicies.size() + " sample policies.");
        }
    }
}
