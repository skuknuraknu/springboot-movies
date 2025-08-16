package com.gugugaga.auth.dto;
import jakarta.validation.constraints.*;

public class CreateUserRequest {

    @NotBlank(message = "Harap memasukkan email")
    private String email;
    @NotBlank(message = "Harap memasukkan username")
    @Size(min = 8, message = "Username minimal 8 karakter")
    private String username;
    @Size(min = 8, message = "Password minimal 8 karakter")
    @NotBlank(message = "Harap memasukkan password")
    private String password;
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
}
