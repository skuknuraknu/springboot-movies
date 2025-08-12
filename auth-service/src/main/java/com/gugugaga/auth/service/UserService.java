package com.gugugaga.auth.service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.*;
import org.springframework.stereotype.Service;

import com.gugugaga.auth.dto.UpdateUserRequest;
import com.gugugaga.auth.entity.User;
import com.gugugaga.auth.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public User createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    public User updateUser( Long userId, UpdateUserRequest req, Long requesterId ) {
        if ( !userId.equals(requesterId) ) {
            throw new AccessDeniedException("Tidak dapat memperbarui data user");
        }
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("User dengan id " + userId + " tidak ditemukan."));
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndIsActiveTrue(id).orElseThrow(() -> new IllegalArgumentException("User dengan id " + id + " tidak ditemukan."));
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsActive(false);
        userRepository.save(user);
    }
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User dengan id " + id + " tidak ditemukan."));
    }
    // Check if email exists
    public Optional<String> findValidationError( User user ) {
        if ( userRepository.existsByUsernameIgnoreCase( user.getUsername() ) ) {
            return Optional.of( "Username sudah terdaftar" );
        }
        if ( userRepository.existsByEmailIgnoreCase( user.getEmail() ) ) {
            return Optional.of( "Email sudah terdaftar" );
        }
        return Optional.empty();
    }
} 
