package com.gugugaga.auth.controller;

import org.springframework.web.bind.annotation.RestController;

import com.gugugaga.auth.dto.AssignRoleRequest;
import com.gugugaga.auth.dto.CreateRoleRequest;
import com.gugugaga.auth.entity.Role;
import com.gugugaga.auth.service.RoleService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequestMapping("/api/auth/role")
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    @PostMapping("/create")
    public ResponseEntity<?> createRole(@Valid @RequestBody CreateRoleRequest request) {
        try {
            Role createdRole = roleService.createRole(request);
            URI location = URI.create("/api/auth/role/create");
            return ResponseEntity.created(location).body(Map.of(
                "success", true,
                "message", "Berhasil membuat role",
                "data", createdRole
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Terjadi kesalahan saat membuat role.",
                "error", e.getMessage()
            ));
        }
    }
    
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @Valid @RequestBody CreateRoleRequest request) {
        try {
            Role updatedRole = roleService.updateRole(id, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Berhasil memperbarui role",
                "data", updatedRole
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Terjadi kesalahan saat memperbarui role.",
                "error", e.getMessage()
            ));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Berhasil menghapus role"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Terjadi kesalahan saat menghapus role.",
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/assign/{userId}")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId, @Valid @RequestBody AssignRoleRequest request) {
        try {
            roleService.insertRoleToUser(userId, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Berhasil memberikan role"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Terjadi kesalahan saat memberikan role.",
                "error", e.getMessage()
            ));
        }
    }

}
