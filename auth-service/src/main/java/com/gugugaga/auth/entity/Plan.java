package com.gugugaga.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_plans")
public class Plan {
    // PRIMARY KEY - Auto-generated unique identifier for each user
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)  // Display name (not necessarily unique)
    private String name;

    @Column(nullable = false)  // Display name (not necessarily unique)
    private String description;

    @Column(name = "max_request", nullable = false)
    private Long maxRequest;

    // AUDIT/TRACKING FIELDS - Important for security and debugging
    @Column(name = "created_at", updatable = false)  // When was this account created? (never changes)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")  // When was this account last modified?
    private LocalDateTime updatedAt;

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

    public Long getMaxRequest() {
        return maxRequest;
    }

    public void setMaxRequest(Long maxRequest) {
        this.maxRequest = maxRequest;
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
}
