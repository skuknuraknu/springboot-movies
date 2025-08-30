package com.gugugaga.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gugugaga.auth.dto.UserSubscriptionResponse;
import com.gugugaga.auth.service.SubscriptionService;

@RestController
@RequestMapping("/api/auth/subscription")
public class SubscriptionController {
    private SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
    
    /**
     * Get user's active subscription for rate limiting
     * 
     * Business Purpose:
     * - Provides gateway with user's current rate limit
     * - Returns plan details for quota enforcement
     * - Used internally by rate limiting system
     * 
     * @param userId User identifier
     * @return Current subscription with plan details
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserSubscriptionResponse> getUserSubscription(@PathVariable Long userId) {
        try {
            UserSubscriptionResponse subscription = subscriptionService.findActiveSubscription( userId );
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            UserSubscriptionResponse subscription = new UserSubscriptionResponse();
            subscription.setLimit(10L);
            return ResponseEntity.ok(subscription); // Empty = free tier
        }
    }
}
