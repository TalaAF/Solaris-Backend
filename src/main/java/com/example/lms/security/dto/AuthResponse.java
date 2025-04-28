package com.example.lms.security.dto;

import com.example.lms.security.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    // Rename accessToken to token to match frontend expectations
    private String token; 
    
    // Replace individual user fields with a nested user object
    private UserInfo user;
    
    // For backward compatibility, you can keep the constructor
    public AuthResponse(String accessToken, String tokenId, Long userId, String email, String roles) {
        this.token = accessToken;
        // Create a basic user info object
        this.user = new UserInfo();
        this.user.setId(userId);
        this.user.setEmail(email);
        // You'll need to parse roles string into set
    }
    
    // Nested class for user information
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String name; // Frontend expects "name"
        private String email;
        private Set<Role> roles; // Frontend expects actual role objects
        private String profileImage; // Frontend expects "profileImage"
    }
}