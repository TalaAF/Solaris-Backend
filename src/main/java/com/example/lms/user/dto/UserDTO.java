package com.example.lms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

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
    // Keep fullName internally but add name getter for frontend compatibility
    private String fullName;
    private Long departmentId;
    private String departmentName;
    private Set<String> roleNames;
    // Keep profilePicture internally but add profileImage getter for frontend compatibility
    private String profilePicture;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Gets the name as expected by frontend.
     * @return The user's full name
     */
    public String getName() {
        return this.fullName;
    }
    
    /**
     * Gets the profile image as expected by frontend.
     * @return The user's profile picture URL
     */
    public String getProfileImage() {
        return this.profilePicture;
    }
}