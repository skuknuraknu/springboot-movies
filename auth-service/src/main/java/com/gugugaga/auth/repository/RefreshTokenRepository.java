package com.gugugaga.auth.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gugugaga.auth.entity.RefreshToken;
import com.gugugaga.auth.entity.User;

import jakarta.transaction.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken( String token );
    Optional<RefreshToken> findByUser(User user);

    // Deactivates tokens for a specific user
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.user = :user")
    int deactivateByUser(User user); // int return number of rows updated

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.expiresAt < :now")
    int deactivateAllExpiredTokens(@Param("now") Instant now);


    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.token = :token")
    int deactivateByToken( String token );
    
    // Find only active tokens
    List<RefreshToken> findAllByIsActive(boolean isActive);
    
}
