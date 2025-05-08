package com.example.lms.user.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    @Email(message = "Email should be valid")
    private String email;
    
    // Keep the field name as fullName for internal consistency
    private String fullName;
    
    private Long departmentId;
    private List<String> roleNames;
    private Boolean isActive; // Use wrapper class to allow null
    // Keep the field name as profilePicture for internal consistency
    private String profilePicture;
    
    /**
     * Setter for 'name' field from frontend that maps to internal fullName
     * @param name The user's name from frontend request
     */
    public void setName(String name) {
        this.fullName = name;
    }
    
    /**
     * Setter for 'profileImage' field from frontend that maps to internal profilePicture
     * @param profileImage The profile image URL from frontend request
     */
    public void setProfileImage(String profileImage) {
        this.profilePicture = profileImage;
    }
    
    // Include both forms for maximum compatibility
    public Boolean isActive() {
        return isActive;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
}