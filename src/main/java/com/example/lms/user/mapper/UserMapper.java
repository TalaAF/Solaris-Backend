package com.example.lms.user.mapper;

import com.example.lms.Department.model.Department;
import com.example.lms.security.model.Role;
import com.example.lms.security.repository.RoleRepository;
import com.example.lms.user.dto.UserCreateRequest;
import com.example.lms.user.dto.UserDTO;
import com.example.lms.user.dto.UserUpdateRequest;
import com.example.lms.user.model.User;

import jakarta.persistence.EntityNotFoundException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * User Mapper
 * 
 * This class handles the conversion between User entities and DTOs.
 * It follows the Mapper pattern to encapsulate object transformation logic.
 * 
 * Responsibilities:
 * 1. Convert User entities to UserDto objects (for API responses)
 * 2. Convert UserCreateRequest objects to User entities (for creation)
 * 3. Update User entities from UserUpdateRequest objects (for updates)
 * 
 * Security features:
 * - Handles password encoding during user creation
 * - Centralizes all object mapping logic in one place
 * - Ensures consistent transformation rules
 */

@Component
public class UserMapper {
    
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    
    public UserMapper(PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }
    
    public UserDTO toDto(User user) {
        UserDTO dto = UserDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .profilePicture(user.getProfilePicture())
            .isActive(user.isActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
        
        // Map roles to role names
        if (user.getRoles() != null) {
            dto.setRoleNames(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        }
        
        // Add department info if available
        if (user.getDepartment() != null) {
            dto.setDepartmentId(user.getDepartment().getId());
            dto.setDepartmentName(user.getDepartment().getName());
        }
        
        return dto;
    }
    
    public User toEntity(UserCreateRequest request, Department department) {
        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .tokenVersion(0L) // Initialize token version
                .profilePicture(request.getProfilePicture())
                .isActive(true)
                .department(department)
                .build();
        
        // Handle roles separately since they can't be directly set in the builder
        Set<Role> roles = new HashSet<>();
        if (request.getRoleNames() != null && !request.getRoleNames().isEmpty()) {
            for (String roleName : request.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
                roles.add(role);
            }
        } else {
            // Default to STUDENT role if none specified
            Role defaultRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new EntityNotFoundException("Default role not found"));
            roles.add(defaultRole);
        }
        user.setRoles(roles);
        
        return user;
    }
    
    // Overloaded method for backward compatibility
    public User toEntity(UserCreateRequest request) {
        return toEntity(request, null);
    }
    
    public void updateEntity(User user, UserUpdateRequest request) {
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }
    }
}