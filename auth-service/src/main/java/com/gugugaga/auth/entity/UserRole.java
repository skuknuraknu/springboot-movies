package com.gugugaga.auth.entity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_user_roles")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // must exist
    private Long id;
    
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "assigned_at", updatable = false)
    private LocalDateTime assignedAt;

    // Constructors
    public UserRole() {}
    
    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
        this.assignedAt = LocalDateTime.now();
    }
    public Role getRole() {
        return role;
    }
    // Getters, setters, equals, hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRole)) return false;
        UserRole userRole = (UserRole) o;
        return Objects.equals(user, userRole.user) &&
               Objects.equals(role, userRole.role);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user, role);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
