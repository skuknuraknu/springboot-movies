package com.gugugaga.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.gugugaga.auth.entity.Role;
import jakarta.validation.constraints.NotEmpty;

public class AssignRoleRequest {
    @NotEmpty(message = "Roles tidak boleh kosong.")
    private List<Long> roles;  // or Set<Role>

    public List<Long> getRoles() {
        return roles;
    }

    public void setRoles(List<Long> roles) {
        this.roles = roles;
    }
}

