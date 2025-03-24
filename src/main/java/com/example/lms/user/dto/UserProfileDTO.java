package com.example.lms.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserProfileDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        private String lastName;
        
        @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
        private String phoneNumber;
        
        @Size(max = 500, message = "Biography cannot exceed 500 characters")
        private String biography;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long userId;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String biography;
        private String profilePictureUrl;
        private boolean isProfileComplete;
    }
}