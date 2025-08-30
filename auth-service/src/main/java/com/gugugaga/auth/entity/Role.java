package com.gugugaga.auth.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

/**
 * Role Entity - Represents user roles/permissions in the system
 * 
 * This entity defines different roles that users can have (e.g., USER, ADMIN, MODERATOR).
 * Roles are used to control access to different parts of the application.
 * 
 * Common roles might include:
 * - USER: Basic user with standard permissions
 * - ADMIN: Administrative access to all features
 * - MODERATOR: Can moderate content but not full admin access
 * - PREMIUM_USER: Enhanced user with additional features
 * 
 * Key features:
 * - Unique role names to prevent duplicates
 * - Description field for human-readable role explanation
 * - Audit fields for tracking when roles are created/modified
 * - Many-to-many relationship with users through UserRole junction table
 * - Active/inactive status for role management
 * 
 * Database table: tb_roles
 * Primary key: id (auto-generated)
 */
@Entity
@Table(name = "tb_roles", uniqueConstraints = @UniqueConstraint(name = "uk_roles_name", columnNames = "name"))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Role {
    
    // PRIMARY KEY - Auto-generated unique identifier for each role
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ROLE IDENTIFICATION
    @Column(name = "name", unique = true, nullable = false, length = 50)  // Role name (e.g., "ADMIN", "USER")
    private String name;

    @Column(name = "description", length = 255)  // Human-readable description of what this role can do
    private String description;

    // AUDIT FIELDS - Track when roles are created and modified
    @Column(name = "created_at", updatable = false)  // When was this role created?
    private LocalDateTime createdAt;

    @Column(name = "updated_at")  // When was this role last modified?
    private LocalDateTime updatedAt;

    @Column(name = "is_active")  // Is this role currently active/usable?
    private Boolean isActive;
    
    // USER RELATIONSHIP
    // One role can be assigned to many users
    // This creates the many-to-many relationship with users through UserRole junction table
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference  // Prevents infinite recursion during JSON serialization
    @JsonIgnore  // Don't include users when serializing role to JSON
    private Set<UserRole> userRoles = new HashSet<>();
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

}
