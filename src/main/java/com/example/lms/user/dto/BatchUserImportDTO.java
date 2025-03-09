package com.example.lms.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class BatchUserImportDTO {
    @NotEmpty(message = "Full name is required")
    private String fullName;
    
    @NotEmpty(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    private Set<String> roleNames;
    
    private Long departmentId;
}