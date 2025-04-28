package com.example.lms.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Email(message = "Email should be valid")
    private String email;
    
    // Keep the field name as fullName for internal consistency
    private String fullName;
    
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
}