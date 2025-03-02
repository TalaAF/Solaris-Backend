package com.example.lms.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Email(message = "Email should be valid")
    private String email;
    
    private String fullName;
    private String profilePicture;
}