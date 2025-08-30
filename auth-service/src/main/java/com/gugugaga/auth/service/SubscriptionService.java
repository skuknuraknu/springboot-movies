package com.gugugaga.auth.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gugugaga.auth.dto.UserSubscriptionResponse;
import com.gugugaga.auth.entity.Subscription;
import com.gugugaga.auth.repository.SubscriptionRepository;

@Service
public class SubscriptionService {
    private SubscriptionRepository subscriptionRepository;
    public SubscriptionService ( SubscriptionRepository subscriptionRepository ) {
        this.subscriptionRepository = subscriptionRepository;
    }
    public UserSubscriptionResponse findActiveSubscription( Long userId ) {
        Optional<Subscription> subscription = subscriptionRepository.findActiveSubscriptionByUserId(userId, LocalDate.now());
        if ( subscription.isPresent() && subscription.get().getPlan() != null ) {
            Subscription sub = subscription.get();
            UserSubscriptionResponse response = new UserSubscriptionResponse();
            response.setLimit(sub.getPlan().getMaxRequest());
            return response;
        } else {
            UserSubscriptionResponse response = new UserSubscriptionResponse();
            response.setLimit(10L);
            return response;
        }
    }
    
}
