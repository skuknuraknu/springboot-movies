package com.gugugaga.gateway.config;

import com.gugugaga.gateway.filter.JwtFilter;

import java.util.Arrays;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final ServiceConfiguration serviceConfig;

    public SecurityConfig(JwtFilter jwtFilter, ServiceConfiguration serviceConfig) {
        this.jwtFilter = jwtFilter;
        this.serviceConfig = serviceConfig;
    }
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Auth Service Routes (with JWT validation, excluding subscription)
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .filters( f -> f.filter( jwtFilter.apply(
                    createJwtConfig(serviceConfig.getSecurity().getPublicEndpoints())
                )))
                .uri(serviceConfig.getUrls().getAuthService())
            )
            // Protected Movie Routes (with JWT validation - bypassed for dev)
            .route("movie-service", r -> r
                .path("/api/movies/**")
                .filters(f -> f.filter(jwtFilter.apply(
                    // createJwtConfig(Arrays.asList("/api/movies/**")) // Add movies as public endpoint for dev
                    createJwtConfig(serviceConfig.getSecurity().getPublicEndpoints())
                )))
                .uri(serviceConfig.getUrls().getMovieService())
            )
            
            // Actuator endpoints
            .route("actuator", r -> r
                .path("/actuator/**")
                .uri(serviceConfig.getUrls().getActuatorService())
            )
            .build();
    }
    
    private JwtFilter.Config createJwtConfig(java.util.List<String> publicEndpoints) {
        JwtFilter.Config config = new JwtFilter.Config();
        config.setPublicEndpoints(publicEndpoints);
        return config;
    }
}