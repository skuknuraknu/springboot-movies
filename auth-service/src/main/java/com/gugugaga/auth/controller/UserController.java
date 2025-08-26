package com.gugugaga.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gugugaga.auth.dto.AssignRoleRequest;
import com.gugugaga.auth.dto.AuthRequest;
import com.gugugaga.auth.dto.CreateUserRequest;
import com.gugugaga.auth.dto.RefreshTokenRequest;
import com.gugugaga.auth.dto.UpdateUserRequest;
import com.gugugaga.auth.entity.RefreshToken;
import com.gugugaga.auth.entity.Role;
import com.gugugaga.auth.entity.User;
import com.gugugaga.auth.entity.UserRole;
import com.gugugaga.auth.mapper.UserMapper;
import com.gugugaga.auth.service.JwtUtil;
import com.gugugaga.auth.service.RefreshTokenService;
import com.gugugaga.auth.service.UserService;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RefreshTokenService refreshTokenService;

    public UserController( UserService userService, JwtUtil jwtUtil ) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest req ) {
        try {
            userService.findValidationError(req).ifPresent(errorMessage -> {
                throw new IllegalArgumentException(errorMessage);
            });
            User createdUser = userService.createUser(req);
            URI location = URI.create("/api/auth/register" + createdUser.getId());
            return ResponseEntity.created(location).body(Map.of(
                    "success", true,
                    "message", "User created successfully",
                    "data", createdUser
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Terjadi kesalahan saat membuat user",
                    "error", e.getMessage()
            ));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {   
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Berhasil menghapus user"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Terjadi kesalahan saat menghapus user",
                    "error", e.getMessage()
            ));
        }
    }
    
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req) {
        try {
            User updatedUser = userService.updateUser(id, req, id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Berhasil memperbarui user",
                "data", updatedUser
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Terjadi kesalahan saat memperbarui user",
                    "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            List<User> user = userService.findUserWithRolesById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User found",
                "data", user
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Terjadi kesalahan saat mengambil user",
                    "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // Authenticate the user
            authenticationManager.authenticate( new UsernamePasswordAuthenticationToken( authRequest.getUsername(),  authRequest.getPassword() ) );
            
            // Load user details
            UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
            
            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken( authRequest.getUsername() );

            // Get user entity for additional info
            User user = userService.findByUsername(authRequest.getUsername());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "data", Map.of(
                    "token", token,
                    "refreshToken", refreshToken.getToken(),
                    "tokenType", "Bearer",
                    "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "isActive", user.getIsActive()
                    )
                )
            ));
            
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Invalid username or password",
                "error", "Authentication failed"
            ));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "User not found",
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "An error occurred during login",
                "error", e.getMessage()
            ));
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String requestRefreshToken = request.getRefreshToken();
            
            // Find the refresh token
            RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken).orElseThrow(() -> new RuntimeException("Refresh token not found"));
            
            // Verify expiration
            refreshToken = refreshTokenService.verifyExpiration(refreshToken);
            
            // Generate new access token
            String accessToken = jwtUtil.generateAccessTokenFromRefreshToken(refreshToken);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token refreshed successfully", 
                "data", Map.of(
                    "accessToken", accessToken,
                    "refreshToken", requestRefreshToken,
                    "tokenType", "Bearer"
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Token refresh failed",
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String token) {
        // Extract username from token
        String jwt = token.substring(7); // Remove "Bearer " prefix
        String username = jwtUtil.extractUsername(jwt);
        
        // Delete refresh token from database
        User user = userService.findByUsername(username);
        refreshTokenService.deleteByUserId(user.getId());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "User logged out successfully"
        ));
    }
}
