package com.gugugaga.gateway.config;

import com.gugugaga.gateway.filter.JwtFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Auth Service Routes (no JWT validation)
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .uri("http://localhost:8085")
            )
            
            // Public Movie Routes (no JWT validation)
            .route("movie-public", r -> r
                .path("/api/movies/public/**")
                .uri("http://localhost:8083")
            )
            
            // Protected Movie Routes (with JWT validation)
            .route("movie-protected", r -> r
                .path("/api/movies/**")
                .filters(f -> f.filter(jwtFilter.apply(
                    createJwtConfig(Arrays.asList()) // No public endpoints for this route
                )))
                .uri("http://localhost:8083")
            )
            
            // Actuator endpoints
            .route("actuator", r -> r
                .path("/actuator/**")
                .uri("http://localhost:8081")
            )
            .build();
    }
    
    private JwtFilter.Config createJwtConfig(java.util.List<String> publicEndpoints) {
        JwtFilter.Config config = new JwtFilter.Config();
        config.setPublicEndpoints(publicEndpoints);
        return config;
    }
}