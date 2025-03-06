package com.example.lms.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {
    private Long id;
    
    @NotBlank(message = "Permission name is required")
    private String name;
    
    private String description;
}