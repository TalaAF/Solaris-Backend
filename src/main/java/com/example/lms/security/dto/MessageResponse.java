package com.example.lms.security.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // Only include non-null fields in JSON
public class MessageResponse {
    private String message;
    
    // Development-only fields, will not be included in production responses
    private String devToken;
    private String devResetUrl;
    
    public MessageResponse(String message) {
        this.message = message;
    }
    
    public void setDevToken(String devToken) {
        this.devToken = devToken;
    }
    
    public void setDevResetUrl(String devResetUrl) {
        this.devResetUrl = devResetUrl;
    }
}