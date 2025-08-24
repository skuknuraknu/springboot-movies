package com.gugugaga.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gugugaga.auth.dto.AssignRoleRequest;
import com.gugugaga.auth.dto.AuthRequest;
import com.gugugaga.auth.dto.CreateUserRequest;
import com.gugugaga.auth.dto.UpdateUserRequest;
import com.gugugaga.auth.entity.Role;
import com.gugugaga.auth.entity.User;
import com.gugugaga.auth.entity.UserRole;
import com.gugugaga.auth.mapper.UserMapper;
import com.gugugaga.auth.service.JwtUtil;
import com.gugugaga.auth.service.UserService;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

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
    
    @PostMapping("/login")
    public ResponseEntity<?> login( @Valid @RequestBody AuthRequest req ) {
        try {
            authenticationManager.authenticate( new UsernamePasswordAuthenticationToken( req.getUsername(), req.getPassword()));
            UserDetails userDetails = userService.loadUserByUsername(req.getUsername());
            String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.status(200).body( Map.of(
                 "success", true,
                "message", "Berhasil login",
                "token", token
            ));
        } catch ( Exception e ) {
            return ResponseEntity.status(500).body( Map.of(
                 "success", false,
                "message", "Terjadi kesalahan saat login",
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getUserById( @PathVariable Long id ) {
        try {
            User user = userService.findById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Berhasil mendapatkan user",
                "data", user
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Terjadi kesalahan saat mendapatkan user",
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/assignRoles/{id}")
    public ResponseEntity<?> assignRole( @PathVariable Long id, @Valid @RequestBody AssignRoleRequest req ) {
        try {
            userService.assignRolesToUser(id, req);
            return ResponseEntity.status(201).body(Map.of(
                "success", true,
                "message", "Berhasil memberikan role"
            ));
        } catch ( Exception e ) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Terjadi kesalahan saat memberikan peran",
                "error", e.getMessage()
            ));
        }
    }
}
