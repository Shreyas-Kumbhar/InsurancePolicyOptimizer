package com.suraksha.shield.controller;

import com.suraksha.shield.entity.Policy;
import com.suraksha.shield.entity.User;
import com.suraksha.shield.repository.UserRepository;
import com.suraksha.shield.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PolicyService policyService;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        User user = getAuthenticatedUser();

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("email", user.getEmail());
        profile.put("allocatedPolicies", user.getAllocatedPolicies());

        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/policies/{policyId}/deallocate")
    public ResponseEntity<?> deallocatePolicy(@PathVariable Long policyId) {
        User user = getAuthenticatedUser();
        Policy policy = policyService.getPolicyById(policyId);

        boolean removed = user.getAllocatedPolicies().removeIf(p -> p.getId().equals(policy.getId()));

        Map<String, String> response = new HashMap<>();
        if (removed) {
            userRepository.save(user);
            response.put("message", "Policy removed from your profile.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "This policy was not allocated to your profile.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername()
                : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
