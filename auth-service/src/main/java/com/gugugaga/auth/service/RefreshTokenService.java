package com.gugugaga.auth.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gugugaga.auth.entity.RefreshToken;
import com.gugugaga.auth.entity.User;
import com.gugugaga.auth.repository.RefreshTokenRepository;
import com.gugugaga.auth.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenDurationMs; // e.g., 7 days = 604800000 ms
    
    @Value("${jwt.refresh.rotation.enabled:true}")
    private boolean rotationEnabled;
    
    @Value("${jwt.refresh.max.rotations:5}")
    private int maxRotations;    

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken createRefreshToken( String username ) {
        // This ensures one active refresh token per user
        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> new RuntimeException("User tidak ditemukan : " + username));
        refreshTokenRepository.deactivateByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt( Instant.now().plusMillis( refreshTokenDurationMs ));
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setRotationCount(0);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken refreshToken ) {
        boolean isExpired = refreshToken.getExpiresAt().compareTo( Instant.now()) < 0;
        if ( isExpired ) {
            refreshTokenRepository.deactivateByToken( refreshToken.getToken() );
            throw new IllegalArgumentException("Token sudah expired");
        }
        System.out.println("is token valid: " + isExpired);
        refreshToken.setLastUsed(Instant.now());
        refreshToken.setUpdatedAt(Instant.now());
        if ( rotationEnabled && refreshToken.getRotationCount() < maxRotations ) {
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setRotationCount(refreshToken.getRotationCount() + 1);
            // Extend expiry on rotation
            refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenDurationMs));
            return refreshTokenRepository.save(refreshToken);
        } else {
            throw new IllegalArgumentException("Tidak dapat mengubah token, batas rotasi tercapai");
        }
    }

    public RefreshToken deleteByUserId( Long userId ) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User tidak ditemukan dengan id : " + userId));
        refreshTokenRepository.deactivateByUser(user);
        return null;
    }
}
