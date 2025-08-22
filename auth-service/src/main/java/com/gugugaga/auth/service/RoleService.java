package com.gugugaga.auth.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gugugaga.auth.dto.AssignRoleRequest;
import com.gugugaga.auth.dto.CreateRoleRequest;
import com.gugugaga.auth.entity.Role;
import com.gugugaga.auth.entity.User;
import com.gugugaga.auth.entity.UserRole;
import com.gugugaga.auth.repository.RoleRepository;
import com.gugugaga.auth.repository.UserRepository;
import com.gugugaga.auth.repository.UserRoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;

    public RoleService( RoleRepository roleRepository, UserRoleRepository userRoleRepository, UserRepository userRepository ) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
    }
    public Role validateRole( CreateRoleRequest request ){
        if ( roleRepository.findByName(request.getName()).isPresent() ) {
            throw new IllegalArgumentException("Role dengan nama: " + request.getName() + " sudah ada.");
        }
        return new Role();
    }
    public Role createRole( CreateRoleRequest request ){
        Role role = validateRole(request);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setCreatedAt(LocalDateTime.now());
        role.setIsActive(true);
        return roleRepository.save(role);
    }
    public Role deleteRole( Long id ){
        Role findRole = roleRepository.findById(id).orElseThrow(() 
            -> new IllegalArgumentException("Role dengan ID: " + id + " tidak ditemukan."));
        findRole.setUpdatedAt(LocalDateTime.now());
        findRole.setIsActive(false);
        return roleRepository.save(findRole);
    }
    public Role updateRole( Long id, CreateRoleRequest request ){
        Role findRole = roleRepository.findById(id).orElseThrow(() 
            -> new IllegalArgumentException("Role dengan ID: " + id + " tidak ditemukan."));
        
        findRole.setName(request.getName());
        findRole.setDescription(request.getDescription());
        findRole.setUpdatedAt(LocalDateTime.now());
        return roleRepository.save(findRole);
    }
    public void insertRoleToUser( Long userId, AssignRoleRequest request ){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User dengan ID: " + userId + " tidak ditemukan."));

        for ( Long roleId : request.getRoleIds() ) {
            Role role = roleRepository.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role dengan ID: " + roleId + " tidak ditemukan."));
            
            // check if already exists
            if ( !userRoleRepository.existsByUserAndRole( user, role ) ) {
                userRoleRepository.save(new UserRole(user, role));
            } else {
                throw new IllegalArgumentException("Role dengan ID: " + roleId + " sudah ditugaskan ke user dengan ID: " + userId);
            }
        }
        userRepository.save(user);
    }
}
