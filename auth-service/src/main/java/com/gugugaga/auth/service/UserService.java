package com.gugugaga.auth.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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

    @Override
    public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
        // Use the new method that fetches roles eagerly
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User dengan username " + username + " tidak ditemukan."));
        
        // Get user's roles from database
        Set<Role> roles = user.getRoles();
        
        List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
            .collect(Collectors.toList());
        
        // If no roles found, add default role
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        System.out.println("Loaded authorities for " + username + ": " + authorities);
        
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            authorities
        );
    }
    
    @Transactional
    public void assignRolesToUser(Long userId, AssignRoleRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        
        List<Role> roles = roleRepository.findAllById(request.getRoles());
        
        for (Role role : roles) {
            // Check if user already has this role
            boolean hasRole = user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getId().equals(role.getId()));
            
            if (!hasRole) {
                UserRole userRole = new UserRole(user, role);
                user.getUserRoles().add(userRole);
            } else {
                // Role already assigned to user
                throw new IllegalArgumentException("Role : " + role.getName() + " sudah diberikan");
            }
        }
        
        userRepository.save(user);
    }
}
