package com.gugugaga.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.gugugaga.auth.filter.JwtFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.gugugaga.auth.service.JwtUtil;

/**
 * Spring Security Configuration Class
 * 
 * This class is the heart of the authentication and authorization system.
 * It defines how Spring Security should handle requests, what endpoints require authentication,
 * and how JWT tokens are processed.
 * 
 * Key concepts:
 * - @Configuration: Marks this as a Spring configuration class
 * - @EnableWebSecurity: Enables Spring Security's web security features
 * - SecurityFilterChain: Defines security rules for HTTP requests
 * - JWT Filter: Custom filter that processes JWT tokens on each request
 * - Stateless sessions: No server-side sessions, everything is in the JWT token
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Dependencies injected via constructor for immutability and better testability
    private final UserDetailsService userDetailsService; // Handles loading user data from database
    private final JwtUtil jwtUtil; // Utility class for JWT token operations

    /**
     * Constructor injection ensures these dependencies are available when the bean is created
     * This is preferred over field injection (@Autowired) for better testability
     */
    public SecurityConfig( JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * Creates our custom JWT filter as a Spring bean
     * This filter will intercept every HTTP request and check for valid JWT tokens
     * It runs BEFORE the standard username/password authentication filter
     */
    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtUtil, userDetailsService);
    }
    
    /**
     * Authentication Manager Bean
     * This is responsible for processing authentication attempts
     * Spring Security uses this to validate user credentials during login
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * Security Filter Chain - The Core Security Configuration
     * This method defines how Spring Security should handle HTTP requests
     * 
     * Key configurations:
     * 1. CSRF disabled - Not needed for stateless JWT authentication
     * 2. Stateless sessions - No server-side sessions, everything in JWT token
     * 3. Custom authentication provider - Uses our database to verify users
     * 4. JWT filter added - Processes JWT tokens on each request
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        http.csrf( csrf -> csrf.disable() ) // Disable CSRF for stateless API
            .sessionManagement( session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No server-side sessions
            ).authenticationProvider(daoAuthenticationProvider(passwordEncoder)) // Use our custom auth provider
        .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class); // Add JWT filter before default auth filter
        return http.build();
    }
    
    /**
     * Database Authentication Provider
     * This configures how Spring Security should authenticate users against the database
     * 
     * Components:
     * - UserDetailsService: Loads user data from database
     * - PasswordEncoder: Handles password hashing/verification (bcrypt, etc.)
     * 
     * When a user tries to log in, this provider:
     * 1. Uses UserDetailsService to find the user in database
     * 2. Uses PasswordEncoder to verify the provided password against stored hash
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider( PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService( userDetailsService ); // How to load user data
        authProvider.setPasswordEncoder( passwordEncoder ); // How to verify passwords
        return authProvider;
    }


}
