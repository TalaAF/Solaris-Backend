package com.example.lms.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenId;  
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String roles;

    public AuthResponse(String accessToken, String tokenId, Long userId, String email, String roles) {
        this.accessToken = accessToken;
        this.tokenId = tokenId;
        this.userId = userId;
        this.email = email;
        this.roles = roles;
    }
}