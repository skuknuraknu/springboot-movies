package com.gugugaga.gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.gugugaga.gateway.dto.UserSubscriptionResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserPlanService {
    private final WebClient webClient;

    @Value("${auth-service.url:http://localhost:8084}")
    private String authServiceUrl;
    
    public UserPlanService() {
        this.webClient = WebClient.builder().build();
    }
    /**
     * Get user's rate limit from their subscription
     * 
     * Business Flow:
     * 1. Call auth-service to get user's active subscription
     * 2. Extract max_request from subscription.plan
     * 3. Cache result for 5 minutes to reduce database load
     * 4. Return default limit if user has no subscription
     * 
     * @param userId - User ID from JWT token or header
     * @return Maximum requests per minute for this user
     */
    @Cacheable(value = "userRateLimits", key = "#userId")
    public Mono<Long> getUserRateLimit(String userId) {
        return webClient.get().uri( authServiceUrl + "/api/auth/subscription/", userId)
        .retrieve().bodyToMono(UserSubscriptionResponse.class)
        .map( res -> {
            if ( res != null && res.getMaxRequest() != null ) {
                Long limit = res.getMaxRequest();
                System.out.println("User max request for id " + userId + " is " + limit);
                return limit;
            } else {
                System.out.println("Error get max request");
                return 10L;
            }
        }).onErrorResume( error -> {
                System.out.println("Failed to fetch user subscription for " + userId );
                System.out.println("Error fetch user subscription" + error.getMessage());
                return Mono.just(10L); // Safe fallback: free tier limit
        });
    }
}
