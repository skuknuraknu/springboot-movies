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

}
