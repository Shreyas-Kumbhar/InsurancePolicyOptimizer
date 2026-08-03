package com.suraksha.shield.controller;

import com.suraksha.shield.dto.PolicyDto;
import com.suraksha.shield.dto.PolicyOptimizationResult;
import com.suraksha.shield.entity.Admin;
import com.suraksha.shield.entity.Policy;
import com.suraksha.shield.entity.User;
import com.suraksha.shield.repository.AdminRepository;
import com.suraksha.shield.repository.UserRepository;
import com.suraksha.shield.service.PolicyService;
import com.suraksha.shield.service.CsvImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

/**
 * Main Thymeleaf view controller.
 * Handles all page routing and server-side rendering — replaces WebMvcConfig,
 * PageController, and all JS fetch-on-load patterns.
 */
@Controller
@RequiredArgsConstructor
public class ViewController {

    private final PolicyService policyService;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final CsvImportService csvImportService;

    // ─────────────────────────────────────────────
    // PUBLIC PAGES
    // ─────────────────────────────────────────────

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "Invalid email or password.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", error);
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Email address already in use.");
            return "redirect:/register";
        }

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMsg", "Registration successful! Please sign in.");
        return "redirect:/login";
    }

    // ─────────────────────────────────────────────
    // POLICY PAGES
    // ─────────────────────────────────────────────

    @GetMapping("/policies")
    public String policiesConfig() {
        return "policies/config";
    }

    @GetMapping("/policies/results")
    public String policiesResults(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(name = "max_premium", required = false) Integer maxPremium,
            @RequestParam(name = "coverage_min", required = false) Integer coverageMin,
            @RequestParam(name = "coverage_max", required = false) Integer coverageMax,
            @RequestParam(name = "show_all", required = false) Boolean showAll,
            Model model) {

        PolicyOptimizationResult result;

        if (Boolean.TRUE.equals(showAll)) {
            List<Policy> all = policyService.getAllPolicies();
            all.sort(Comparator.comparingInt(Policy::getPremium));
            result = new PolicyOptimizationResult(all, false, 0, 0);
        } else {
            result = policyService.optimize(type, riskLevel, name, maxPremium, coverageMin, coverageMax);
        }

        model.addAttribute("result", result);
        model.addAttribute("policies", result.getPolicies());
        model.addAttribute("combinationMode", result.isCombinationMode());
        model.addAttribute("totalPremium", result.getTotalPremium());
        model.addAttribute("totalCoverage", result.getTotalCoverage());
        return "policies/results";
    }

    @GetMapping("/policies/{id:\\d+}")
    public String policyDetail(@PathVariable Long id, Model model) {
        Policy policy;
        try {
            policy = policyService.getPolicyById(id);
        } catch (Exception e) {
            List<Policy> all = policyService.getAllPolicies();
            if (all.isEmpty()) {
                return "redirect:/policies";
            }
            policy = all.get(0);
        }
        
        double ratio = (policy.getCoverage() > 0)
                ? ((double) policy.getPremium() / policy.getCoverage()) * 100 : 0;
        model.addAttribute("policy", policy);
        model.addAttribute("ratio", String.format("%.2f", ratio));
        return "policies/detail";
    }

    @PostMapping("/policies/{id:\\d+}/allocate")
    public String allocatePolicy(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Policy policy = policyService.getPolicyById(id);

        boolean alreadyAllocated = user.getAllocatedPolicies().stream()
                .anyMatch(p -> p.getId().equals(policy.getId()));

        if (alreadyAllocated) {
            redirectAttributes.addFlashAttribute("errorMsg", "You have already allocated this policy.");
        } else {
            user.getAllocatedPolicies().add(policy);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMsg", "Policy successfully allocated to your profile!");
        }
        return "redirect:/profile";
    }

    // ─────────────────────────────────────────────
    // USER PROFILE PAGE
    // ─────────────────────────────────────────────

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("policies", user.getAllocatedPolicies());
        return "profile";
    }

    @PostMapping("/profile/deallocate/{policyId}")
    public String deallocatePolicy(
            @PathVariable Long policyId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean removed = user.getAllocatedPolicies().removeIf(p -> p.getId().equals(policyId));
        if (removed) {
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMsg", "Policy removed from your profile.");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Policy was not found in your profile.");
        }
        return "redirect:/profile";
    }

    // ─────────────────────────────────────────────
    // ADMIN PAGES
    // ─────────────────────────────────────────────

    @GetMapping("/admin/login")
    public String adminLoginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "Invalid admin credentials.");
        }
        return "admin/login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Model model) {
        Page<Policy> policyPage = policyService.getPaginatedPolicies(page, size);
        model.addAttribute("policyPage", policyPage);
        model.addAttribute("policies", policyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", policyPage.getTotalPages());
        model.addAttribute("totalElements", policyPage.getTotalElements());
        return "admin/dashboard";
    }

    @GetMapping("/admin/policies/new")
    public String newPolicyPage() {
        return "admin/newPolicy";
    }

    @PostMapping("/admin/policies/new")
    public String createPolicy(
            @RequestParam String name,
            @RequestParam String provider,
            @RequestParam String type,
            @RequestParam String riskLevel,
            @RequestParam Integer premium,
            @RequestParam Integer coverage,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Admin admin = adminRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        PolicyDto dto = PolicyDto.builder()
                .name(name).provider(provider).type(type)
                .riskLevel(riskLevel).premium(premium).coverage(coverage)
                .build();
        policyService.createPolicy(dto, admin);
        redirectAttributes.addFlashAttribute("successMsg", "Policy created successfully!");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/policies/{id:\\d+}/edit")
    public String editPolicyPage(@PathVariable Long id, Model model) {
        Policy policy = policyService.getPolicyById(id);
        model.addAttribute("policy", policy);
        return "admin/editPolicy";
    }

    @PostMapping("/admin/policies/{id:\\d+}/edit")
    public String updatePolicy(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String provider,
            @RequestParam String type,
            @RequestParam String riskLevel,
            @RequestParam Integer premium,
            @RequestParam Integer coverage,
            RedirectAttributes redirectAttributes) {

        PolicyDto dto = PolicyDto.builder()
                .name(name).provider(provider).type(type)
                .riskLevel(riskLevel).premium(premium).coverage(coverage)
                .build();
        policyService.updatePolicy(id, dto);
        redirectAttributes.addFlashAttribute("successMsg", "Policy updated successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/policies/{id:\\d+}/delete")
    public String deletePolicy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        policyService.deletePolicy(id);
        redirectAttributes.addFlashAttribute("successMsg", "Policy deleted successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/policies/import")
    @ResponseBody
    public ResponseEntity<?> importPoliciesFromCsv(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        Admin admin = adminRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        Map<String, Object> response = csvImportService.importCsv(file, admin);
        return ResponseEntity.ok(response);
    }
}
