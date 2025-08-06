package com.gugugaga.auth.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_roles")
public class Role {
    @Id @GeneratedValue( strategy = GenerationType.IDENTITY )
    private long id;

    @NotBlank(message = "Harap memasukkan nama role")
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @NotBlank(message = "Harap memasukkan deskripsi role")
    @Column(name = "description")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

}
