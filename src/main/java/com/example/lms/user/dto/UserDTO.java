package com.example.lms.user.dto;

import com.example.lms.user.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Data Transfer Object (DTO)
 * 
 * This class is used to transfer user data between the service layer and the presentation layer.
 * It contains all user information that can be safely exposed to the client,
 * excluding sensitive information like passwords.
 * 
 * Key benefits:
 * - Security: Prevents accidental exposure of sensitive data
 * - Flexibility: Can be tailored to specific view requirements
 * - Decoupling: Separates API contracts from internal domain models
 */
@Data
@Builder            // Lombok: Enables the builder pattern for object creation
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private String profilePicture;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}