package com.suraksha.shield.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Serves static HTML pages for clean URL routing.
 * Uses redirect instead of forward to avoid InternalResourceView resolution errors.
 */
@Controller
public class PageController {

    @GetMapping("/policies/{id}")
    public String policyDetail(@PathVariable String id) {
        return "redirect:/policies/detail.html";
    }

    @GetMapping("/admin/policies/{id}/edit")
    public String editPolicy(@PathVariable String id) {
        return "redirect:/admin/editPolicy.html";
    }
}
