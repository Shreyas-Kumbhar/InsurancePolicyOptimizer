package com.suraksha.shield.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Serves static HTML pages for clean URL routing.
 * Uses forward with numeric regex to avoid redirect loops and path variable parsing issues.
 */
@Controller
public class PageController {

    @GetMapping("/policies/{id:\\d+}")
    public String policyDetail(@PathVariable String id) {
        return "forward:/policies/detail.html";
    }

    @GetMapping("/admin/policies/{id:\\d+}/edit")
    public String editPolicy(@PathVariable String id) {
        return "forward:/admin/editPolicy.html";
    }
}
