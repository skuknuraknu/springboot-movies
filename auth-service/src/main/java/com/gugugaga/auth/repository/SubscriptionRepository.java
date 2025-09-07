package com.gugugaga.auth.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gugugaga.auth.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    /**
     * Find user's active subscription with plan details
     * 
     * Business Logic:
     * - Only returns ACTIVE subscriptions (not expired or cancelled)
     * - Checks that subscription hasn't passed its end date
     * - Joins with Plan table to get max_request limit for rate limiting
     */
    @Query("SELECT s FROM Subscription s JOIN FETCH s.plan p WHERE s.user.id = :userId AND s.status = 'ACTIVE' AND (s.endDate IS NULL OR s.endDate >= :currentDate)")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);
}


