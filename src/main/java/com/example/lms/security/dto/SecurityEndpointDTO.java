package com.example.lms.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEndpointDTO {
    private Long id;
    
    @NotBlank(message = "HTTP method is required")
    private String httpMethod;
    
    @NotBlank(message = "Path pattern is required")
    private String pathPattern;
    
    @NotNull(message = "Required permission is required")
    private Long permissionId;
    
    private String permissionName; // For response only
}