package com.gugugaga.auth.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User Entity - Represents a user in the authentication system
 * 
 * This is a JPA entity that maps to the "tb_users" database table.
 * It contains all user-related information including authentication data,
 * audit timestamps, and relationships to roles.
 * 
 * Key features:
 * - Unique constraints on email and username to prevent duplicates
 * - Password field is write-only (won't be serialized to JSON responses)
 * - Audit fields (created_at, updated_at, etc.) for tracking changes
 * - Many-to-many relationship with roles through UserRole junction table
 * - Account status fields (isActive, isEmailVerified) for account management
 * 
 * Database table: tb_users
 * Primary key: id (auto-generated)
 */
@Entity
@Table(name = "tb_users",
uniqueConstraints = {
    @UniqueConstraint(name="uk_users_email", columnNames = "email"),
    @UniqueConstraint(name="uk_users_username", columnNames = "username")
}
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Prevents JSON serialization issues with Hibernate lazy loading
public class User {
    
    // PRIMARY KEY - Auto-generated unique identifier for each user
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // AUTHENTICATION FIELDS
    @Column(name = "email", unique = true, nullable = false)  // Unique email address for login
    private String email;

    @Column(name = "username", nullable = false)  // Display name (not necessarily unique)
    private String username;

    @Column(name = "password", nullable = false)  // Hashed password (never store plain text!)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // Only allow writing, never read in JSON responses
    private String password;

    // ACCOUNT STATUS FIELDS
    @Column(name = "is_active")  // Can the user log in? (for account suspension)
    private Boolean isActive = true;

    @Column(name = "is_email_verified")  // Has the user verified their email address?
    private Boolean isEmailVerified = false;

    // AUDIT/TRACKING FIELDS - Important for security and debugging
    @Column(name = "created_at", updatable = false)  // When was this account created? (never changes)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")  // When was this account last modified?
    private LocalDateTime updatedAt;

    @Column(name = "password_changed_at")  // When was the password last changed? (for security policies)
    private LocalDateTime passwordChangedAt;

    @Column(name = "last_login_at")  // When did the user last log in? (for security monitoring)
    private LocalDateTime lastLoginAt;

    // ROLE RELATIONSHIP
    // One user can have many roles (USER, ADMIN, MODERATOR, etc.)
    // This creates a many-to-many relationship through the UserRole junction table
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference  // Prevents infinite recursion in JSON serialization
    @JsonIgnore  // Don't include roles in user JSON response (fetch separately if needed)
    private Set<UserRole> userRoles = new HashSet<>();

    // SUBSCRIPTION RELATIONSHIP
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @JsonIgnore
    private Set<Subscription> userSubs = new HashSet<>();

    /**
     * Convenience method to get all roles for this user
     * Extracts Role entities from the UserRole junction table
     * Used by Spring Security to determine user permissions
     */
    public Set<Role> getRoles() {
        Set<Role> roles = new HashSet<>();
        for( UserRole ur : userRoles ) {
            roles.add( ur.getRole() );
        }
        return roles;
    }
    public Set<Plan> getSubs(){
        Set<Plan> plans = new HashSet<>();
        for ( Subscription us : userSubs ) {
            plans.add(us.getPlan());
        }
        return plans;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
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

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public void addRole( Role role, String assignedBy ) {
        UserRole userRole = new UserRole(this, role );
        userRoles.add(userRole);
        role.getUserRoles().add(userRole);
    }

}
