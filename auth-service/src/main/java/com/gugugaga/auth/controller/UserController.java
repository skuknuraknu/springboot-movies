package com.gugugaga.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gugugaga.auth.dto.UpdateUserRequest;
import com.gugugaga.auth.entity.User;
import com.gugugaga.auth.service.UserService;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.Map;

import org.springframework.http.ResponseEntity;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user ) {
        try {
            userService.findValidationError(user).ifPresent(errorMessage -> {
                throw new IllegalArgumentException(errorMessage);
            });
            User createdUser = userService.createUser(user);
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
    
}
