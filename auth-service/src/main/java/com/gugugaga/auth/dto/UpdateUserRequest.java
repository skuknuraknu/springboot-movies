package com.gugugaga.auth.dto;

import jakarta.validation.constraints.*;

public class UpdateUserRequest {
    @Size(min = 8, message = "Username minimal 8 karakter")
    private String username;

    @Email(message = "Format email tidak valid")
    private String email;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    
}
