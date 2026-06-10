package com.suraksha.shield.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve all static files from /static/
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map clean URLs to static HTML files using redirect
        registry.addRedirectViewController("/login", "/login.html");
        registry.addRedirectViewController("/register", "/register.html");
        registry.addRedirectViewController("/profile", "/profile.html");
        registry.addRedirectViewController("/policies", "/policies/config.html");
        registry.addRedirectViewController("/policies/results", "/policies/results.html");
        registry.addRedirectViewController("/admin/login", "/admin/login.html");
        registry.addRedirectViewController("/admin/dashboard", "/admin/dashboard.html");
        registry.addRedirectViewController("/admin/policies/new", "/admin/newPolicy.html");
    }
}
