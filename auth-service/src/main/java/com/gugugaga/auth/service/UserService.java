package com.gugugaga.auth.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gugugaga.auth.dto.AssignRoleRequest;
import com.gugugaga.auth.dto.CreateUserRequest;
import com.gugugaga.auth.dto.UpdateUserRequest;
import com.gugugaga.auth.entity.Role;
import com.gugugaga.auth.entity.User;
import com.gugugaga.auth.entity.UserRole;
import com.gugugaga.auth.mapper.UserMapper;
import com.gugugaga.auth.repository.RoleRepository;
import com.gugugaga.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.transaction.Transactional;

@Service
public class UserService implements UserDetailsService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // Inject PasswordEncoder using @Autowired instead
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public UserService(UserMapper userMapper, UserRepository userRepository, RoleRepository roleRepository ) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
    public User createUser(CreateUserRequest req ) {
        User user = userMapper.toEntityUser(req);
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
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
    public List<User> findUserWithRolesById( Long id ) {
        User user = userRepository.findByIdAndIsActiveTrue(id).orElseThrow(() -> new IllegalArgumentException("User dengan id " + id + " tidak ditemukan."));
        return List.of(user);
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user entity from DB with roles
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Convert roles to authorities
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
            .collect(Collectors.toList());
        
        // If no roles found, add default role
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        System.out.println("Loaded authorities for " + username + ": " + authorities);

        // Return Spring Security's User object (correct syntax)
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            authorities  // Pass SimpleGrantedAuthority list, not Role objects
        );
    }
}

