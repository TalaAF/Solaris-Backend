package com.example.lms.security.dto;

import com.example.lms.security.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    // Rename accessToken to token to match frontend expectations
    private String token; 
    
    // Add refresh token field
    private String refreshToken;
    
    // Replace individual user fields with a nested user object
    private UserInfo user;
    
    // Update the constructor to include refreshToken
    public AuthResponse(String accessToken, String refreshToken, String tokenId, Long userId, String email, String roleNames) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        // Create a basic user info object
        this.user = new UserInfo();
        this.user.setId(userId);
        this.user.setEmail(email);
        
        // Parse roles string into set of Role objects
        if (roleNames != null && !roleNames.isEmpty()) {
            Set<Role> roles = Arrays.stream(roleNames.split(","))
                .map(roleName -> {
                    Role role = new Role();
                    role.setName(roleName.trim());
                    return role;
                })
                .collect(Collectors.toSet());
            this.user.setRoles(roles);
        }
    }
    
    // Update the full constructor to include refreshToken
    public AuthResponse(String accessToken, String refreshToken, String tokenId, Long userId, String email, 
                       String fullName, String roleNames, String profilePicture) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.user = new UserInfo();
        this.user.setId(userId);
        this.user.setEmail(email);
        this.user.setName(fullName);
        this.user.setProfileImage(profilePicture);
        
        // Parse roles string into set of Role objects
        if (roleNames != null && !roleNames.isEmpty()) {
            Set<Role> roles = Arrays.stream(roleNames.split(","))
                .map(roleName -> {
                    Role role = new Role();
                    role.setName(roleName.trim());
                    return role;
                })
                .collect(Collectors.toSet());
            this.user.setRoles(roles);
        }
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