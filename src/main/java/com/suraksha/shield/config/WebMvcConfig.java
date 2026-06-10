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
        // Map clean URLs to static HTML files using forward (keeps URLs clean)
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/register").setViewName("forward:/register.html");
        registry.addViewController("/profile").setViewName("forward:/profile.html");
        registry.addViewController("/policies").setViewName("forward:/policies/config.html");
        registry.addViewController("/policies/results").setViewName("forward:/policies/results.html");
        registry.addViewController("/admin/login").setViewName("forward:/admin/login.html");
        registry.addViewController("/admin/dashboard").setViewName("forward:/admin/dashboard.html");
        registry.addViewController("/admin/policies/new").setViewName("forward:/admin/newPolicy.html");
    }
}
