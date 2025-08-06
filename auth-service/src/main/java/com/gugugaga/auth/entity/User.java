package com.gugugaga.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "tb_users")
public class User {
    @Id @GeneratedValue( strategy = GenerationType.IDENTITY )
    private long id;

    @NotBlank(message = "Harap memasukkan email")
    private String email;

    @NotBlank(message = "Harap memasukkan username")
    private String username;

    @NotBlank(message = "Harap memasukkan password")
    private String password;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
