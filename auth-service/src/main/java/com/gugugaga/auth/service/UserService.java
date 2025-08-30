package com.gugugaga.auth.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.gugugaga.auth.dto.CreateUserRequest;
import com.gugugaga.auth.dto.UpdateUserRequest;
import com.gugugaga.auth.entity.User;
import com.gugugaga.auth.mapper.UserMapper;
import com.gugugaga.auth.repository.RoleRepository;
import com.gugugaga.auth.repository.UserRepository;
import com.gugugaga.auth.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.transaction.Transactional;

/**
 * User Service - Core business logic for user management
 * 
 * This service class handles all user-related operations including:
 * - User creation, update, and deletion (soft delete)
 * - User authentication via UserDetailsService interface
 * - Validation (checking for duplicate emails/usernames)
 * - Password encoding for security
 * - Loading user data with roles for Spring Security
 * 
 * Key responsibilities:
 * 1. CRUD operations on User entities
 * 2. Business logic validation (unique email/username)
 * 3. Security integration (implements UserDetailsService)
 * 4. Password hashing using PasswordEncoder
 * 5. Role and permission management
 * 
 * Security notes:
 * - Passwords are always hashed, never stored in plain text
 * - Soft delete is used instead of hard delete for audit trail
 * - Access control ensures users can only modify their own data
 */
@Service
public class UserService implements UserDetailsService {
    // DEPENDENCY INJECTION - Constructor injection for better testability
    private final UserMapper userMapper;        // Converts between DTOs and entities
    private final UserRepository userRepository; // Database operations for users
    private final RoleRepository roleRepository; // Database operations for roles

    // PasswordEncoder injected separately to avoid circular dependencies
    @Autowired
    private PasswordEncoder passwordEncoder;    // Handles password hashing (bcrypt)
    
    public UserService(UserMapper userMapper, UserRepository userRepository, RoleRepository roleRepository ) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
    
    /**
     * Create New User
     * 
     * Process:
     * 1. Convert DTO to entity using mapper
     * 2. Hash the password for security (never store plain text!)
     * 3. Set creation timestamp for audit tracking
     * 4. Save to database
     * 
     * Security: Password is hashed using PasswordEncoder (typically bcrypt)
     * Audit: Creation timestamp is recorded for tracking
     */
    public User createUser(CreateUserRequest req ) {
        User user = userMapper.toEntityUser(req);
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(req.getPassword())); // Hash password for security
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
    /**
     * Load User By Username - Critical method for Spring Security authentication
     * 
     * This method is called by Spring Security during login to:
     * 1. Find the user in the database by username
     * 2. Load all their roles and convert them to Spring Security authorities
     * 3. Return UserDetails object that Spring Security can use for authentication
     * 
     * Process:
     * 1. Query database for user with roles (using eager fetch to avoid lazy loading issues)
     * 2. Convert user roles to Spring Security authorities (prefixed with "ROLE_")
     * 3. Provide default "USER" role if no roles are assigned
     * 4. Return CustomUserDetails with user ID, username, password, and authorities
     * 
     * Security note: Password in UserDetails is used for authentication comparison
     * Authority format: "ROLE_ADMIN", "ROLE_USER", etc. (Spring Security convention)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Use the repository method that fetches roles eagerly to avoid lazy loading issues
        User user = userRepository.findByUsernameWithRoles(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Convert user roles to Spring Security authorities
        // Spring Security expects authorities to be prefixed with "ROLE_"
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
            .collect(Collectors.toList());
        
        // Provide default role if user has no roles assigned (safety measure)
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        System.out.println("Loaded authorities for " + username + ": " + authorities);
        
        // Return our custom UserDetails implementation that includes user ID
        // This is important because we need the user ID in JWT tokens
        return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), authorities);
    }
    public User getCurrentUser() {
        // Get the currently authenticated user from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String username = authentication.getName();
        return findByUsername(username);
    }
    public User findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new IllegalArgumentException("User with username " + username + " not found"));
    }
}

