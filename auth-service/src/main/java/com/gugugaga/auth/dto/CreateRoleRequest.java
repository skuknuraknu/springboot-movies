package com.gugugaga.auth.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateRoleRequest {
    
    @NotBlank(message = "Harap memasukkan nama role")
    @Size(max = 50, message = "Nama role maksimal 50 karakter")
    private String name;

    @Size(max = 255, message = "Deskripsi role maksimal 255 karakter")
    private String description;

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
}
