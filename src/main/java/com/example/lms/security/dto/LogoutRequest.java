package com.example.lms.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequest {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}