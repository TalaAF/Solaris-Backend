package com.example.lms.user.dto;

import java.util.HashSet;
import java.util.Set;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * User Creation Request DTO
 * 
 * This class defines the contract for creating a new user in the system.
 * It contains all required fields with validation constraints to ensure data integrity.
 * 
 * Purpose:
 * - Defines which fields are required for user creation
 * - Applies validation rules using Bean Validation annotations
 * - Separates API contract from internal domain model
 * - Provides clear documentation of API requirements
 * 
 * This class works with the UserController and UserService for creating new users.
 */
@Data
public class UserCreateRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private Set<String> roleNames = new HashSet<>();

    private String profilePicture;
}