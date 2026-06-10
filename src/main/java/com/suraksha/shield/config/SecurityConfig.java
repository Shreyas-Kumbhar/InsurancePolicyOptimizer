package com.suraksha.shield.config;

import jakarta.servlet.DispatcherType;
import com.suraksha.shield.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow FORWARD dispatches (internal forwards from ViewControllers) without re-checking
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                // All static files and page routes are public (HTML, CSS, JS)
                .requestMatchers(
                    "/", "/index.html",
                    "/login", "/login.html",
                    "/register", "/register.html",
                    "/profile", "/profile.html",
                    "/policies", "/policies/**",
                    "/admin/login", "/admin/login.html",
                    "/admin/dashboard", "/admin/dashboard.html",
                    "/admin/policies/**",
                    "/admin/**",
                    "/css/**", "/js/**", "/favicon.ico", "/*.html"
                ).permitAll()
                // Auth APIs are public
                .requestMatchers("/api/auth/**").permitAll()
                // Policy results/search is public (must be listed BEFORE the {id} wildcard rule)
                .requestMatchers(HttpMethod.GET, "/api/policies/results").permitAll()
                // Policy GET by id is public
                .requestMatchers(HttpMethod.GET, "/api/policies/{id}").permitAll()
                // User profile & policy allocation require ROLE_USER
                .requestMatchers("/api/users/profile").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/policies/{id}/allocate").hasRole("USER")
                // Admin API actions require ROLE_ADMIN
                .requestMatchers(HttpMethod.POST, "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/admin/**").hasRole("ADMIN")
                // All other requests must be authenticated
                .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
