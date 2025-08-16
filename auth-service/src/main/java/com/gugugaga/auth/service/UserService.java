package com.gugugaga.auth.service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.*;
import org.springframework.stereotype.Service;

import com.gugugaga.auth.dto.CreateUserRequest;
import com.gugugaga.auth.dto.UpdateUserRequest;
import com.gugugaga.auth.entity.User;
import com.gugugaga.auth.mapper.UserMapper;
import com.gugugaga.auth.repository.UserRepository;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserService(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }
    public User createUser(CreateUserRequest req) {
        User user = userMapper.toEntityUser(req);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    public User updateUser( Long userId, UpdateUserRequest req, Long requesterId ) {
        if ( !userId.equals(requesterId) )
            throw new AccessDeniedException("Tidak dapat memperbarui data user");
        User existingUser = userRepository.findByIdAndIsActiveTrue(userId).orElseThrow(() -> new IllegalArgumentException("User dengan id " + userId + " tidak ditemukan."));
        
        if ( req.getEmail() != null && !req.getEmail().equals(existingUser.getEmail()) ) {
            if ( userRepository.existsByEmailIgnoreCase( req.getEmail() ) ) {
                throw new IllegalArgumentException("Email sudah terdaftar");
            }
            existingUser.setEmail(req.getEmail());
        }
        if ( req.getUsername() != null && !req.getUsername().equals(existingUser.getUsername()) ) {
            if ( userRepository.existsByUsernameIgnoreCase( req.getUsername() ) ) {
                throw new IllegalArgumentException("Username sudah terdaftar");
            }
            existingUser.setUsername(req.getUsername());
        }
        userMapper.updateEntityFromDto(req, existingUser);
        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
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
    public Optional<String> findValidationError( CreateUserRequest user ) {
        if ( userRepository.existsByUsernameIgnoreCase( user.getUsername() ) ) {
            return Optional.of( "Username sudah terdaftar" );
        }
        if ( userRepository.existsByEmailIgnoreCase( user.getEmail() ) ) {
            return Optional.of( "Email sudah terdaftar" );
        }
        return Optional.empty();
    }
} 
