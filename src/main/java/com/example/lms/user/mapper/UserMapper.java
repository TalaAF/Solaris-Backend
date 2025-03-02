package com.example.lms.user.mapper;

import com.example.lms.user.dto.UserCreateRequest;
import com.example.lms.user.dto.UserDTO;
import com.example.lms.user.dto.UserUpdateRequest;
import com.example.lms.user.model.User;
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
    
    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    public UserDTO toDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .profilePicture(user.getProfilePicture())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    public User toEntity(UserCreateRequest request) {
        return User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .profilePicture(request.getProfilePicture())
                .isActive(true)
                .build();
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