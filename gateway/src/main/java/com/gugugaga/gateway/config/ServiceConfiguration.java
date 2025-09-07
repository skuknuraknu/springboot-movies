package com.gugugaga.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.services")
@EnableConfigurationProperties
public class ServiceConfiguration {
    
    private ServiceUrls urls = new ServiceUrls();
    private RateLimiting rateLimiting = new RateLimiting();
    private Security security = new Security();
    
    @Data
    public static class ServiceUrls {
        private String authService = "http://localhost:8084";
        private String movieService = "http://localhost:8085"; 
        private String actuatorService = "http://localhost:8086";
    }
    
    @Data
    public static class RateLimiting {
        private CacheConfig cache = new CacheConfig();
        private ServiceLimits serviceLimits = new ServiceLimits();
        private Messages messages = new Messages();
        
        @Data
        public static class CacheConfig {
            private long expireAfterWriteHours = 1;
            private int maximumSize = 10000;
        }
        
        @Data
        public static class ServiceLimits {
            private int authRequestsPerMinute = 100;
            private int movieRequestsPerMinute = 100;
            private int defaultRequestsPerMinute = 5;
        }
        
        @Data
        public static class Messages {
            private String upgradeMessageBasic = "Rate limit exceeded. Upgrade to Premium for higher limits!";
            private String upgradeMessagePremium = "Rate limit exceeded. Please try again in 1 minute.";
            private long retryAfterSeconds = 60;
        }
    }
    
    @Data
    public static class Security {
        private List<String> publicEndpoints = List.of(
            "/api/auth/login",
            "/api/auth/register", 
            "/api/auth/refresh",
            "/api/auth/subscription/**"
        );
        private List<String> corsOrigins = List.of("http://localhost:3000");
        private List<String> corsHeaders = List.of("Range", "Content-Type", "Accept", "Origin", "X-Requested-With", "X-User-Id");
        private List<String> corsMethods = List.of("GET", "HEAD", "OPTIONS", "POST", "PUT", "DELETE");
    }
}