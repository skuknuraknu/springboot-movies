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

/**
 * User Controller - REST API endpoints for authentication and user management
 * 
 * This controller handles all HTTP requests related to user authentication and management.
 * It serves as the entry point for client applications to interact with the auth service.
 * 
 * Main endpoints:
 * - POST /api/auth/register: Create new user account
 * - POST /api/auth/login: Authenticate user and return JWT tokens
 * - POST /api/auth/refresh: Refresh expired access tokens
 * - POST /api/auth/logout: Invalidate user session
 * - GET /api/auth/get/{id}: Retrieve user information
 * - PUT /api/auth/update/{id}: Update user information
 * - DELETE /api/auth/{id}: Soft delete user account
 * 
 * Security features:
 * - JWT-based authentication (stateless)
 * - Refresh token mechanism for token renewal
 * - Password validation and hashing
 * - Proper HTTP status codes and error handling
 * - Request validation using @Valid annotation
 * 
 * Response format:
 * All endpoints return JSON with consistent structure:
 * {
 *   "success": boolean,
 *   "message": string,
 *   "data": object,
 *   "error": string (only on errors)
 * }
 */
@RestController
@RequestMapping("/api/auth")
public class UserController {
    // DEPENDENCY INJECTION
    private final UserService userService;          // Handles user business logic
    private final JwtUtil jwtUtil;                 // Generates and validates JWT tokens
    
    @Autowired
    private AuthenticationManager authenticationManager; // Spring Security authentication
    
    @Autowired
    private RefreshTokenService refreshTokenService;     // Manages refresh tokens

    public UserController( UserService userService, JwtUtil jwtUtil ) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * User Registration Endpoint
     * 
     * Creates a new user account with validation.
     * 
     * Process:
     * 1. Validate request data (@Valid annotation triggers validation)
     * 2. Check for duplicate email/username
     * 3. Create user with hashed password
     * 4. Return success response with user data
     * 
     * HTTP Method: POST
     * Path: /api/auth/register
     * Request: CreateUserRequest JSON in body
     * Response: User data with success/error message
     * Status Codes: 201 (Created), 500 (Server Error)
     */
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
    
    /**
     * User Login Endpoint - The heart of authentication
     * 
     * Authenticates a user and returns JWT tokens for session management.
     * This is one of the most critical endpoints in the system.
     * 
     * Process:
     * 1. Validate credentials using Spring Security AuthenticationManager
     * 2. Load user details (including roles) from database
     * 3. Generate JWT access token (short-lived, ~1 hour)
     * 4. Generate refresh token (long-lived, ~7 days) stored in database
     * 5. Return both tokens + user info to client
     * 
     * Security features:
     * - Password verification (bcrypt hashing)
     * - Role-based authorities loaded for authorization
     * - Refresh token mechanism for seamless token renewal
     * - Proper error handling for security (don't leak info)
     * 
     * HTTP Method: POST
     * Path: /api/auth/login
     * Request: { "username": "user", "password": "pass" }
     * Response: JWT tokens + user data
     * Status Codes: 200 (Success), 401 (Invalid credentials), 404 (User not found), 500 (Server error)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // Step 1: Authenticate credentials using Spring Security
            // This will throw BadCredentialsException if username/password is wrong
            authenticationManager.authenticate( 
                new UsernamePasswordAuthenticationToken( 
                    authRequest.getUsername(),  
                    authRequest.getPassword() 
                ) 
            );
            
            // Step 2: Load user details (including roles) for token generation
            UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
            
            // Step 3: Generate JWT access token (contains user info + roles)
            String token = jwtUtil.generateToken(userDetails);
            
            // Step 4: Generate refresh token (stored in database for security)
            RefreshToken refreshToken = refreshTokenService.createRefreshToken( authRequest.getUsername() );

            // Step 5: Get user entity for response data
            User user = userService.findByUsername(authRequest.getUsername());
            
            // Step 6: Return success response with tokens and user info
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "data", Map.of(
                    "token", token,                        // JWT access token for API calls
                    "refreshToken", refreshToken.getToken(), // Refresh token for token renewal
                    "tokenType", "Bearer",                 // Standard OAuth 2.0 token type
                    "user", Map.of(                        // User information for UI
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "isActive", user.getIsActive()
                    )
                )
            ));
            
        } catch (BadCredentialsException e) {
            // Invalid username or password - return 401 Unauthorized
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Invalid username or password",
                "error", "Authentication failed"
            ));
        } catch (UsernameNotFoundException e) {
            // User doesn't exist - return 404 Not Found
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "User not found",
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            // Any other error - return 500 Internal Server Error
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
