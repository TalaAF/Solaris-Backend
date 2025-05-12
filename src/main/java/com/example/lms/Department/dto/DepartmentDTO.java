package com.example.lms.Department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DepartmentDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Department name is required")
        @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
        private String name;
        
        @NotBlank(message = "Department code is required")
        @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "Department code must be 2-10 uppercase letters or numbers")
        private String code;
        
        private String description;
        private boolean active = true;
        private String specialtyArea;
        private String contactInformation;
        
        // Add this field
        private Long headId;
        private String headOfDepartment;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String code;
        private String description;
        private boolean active;
        private String specialtyArea;
        private String contactInformation;
        private HeadDTO head; // Department head
        private Long userCount; // Number of users in department
        private Long courseCount; // Make sure this is Long, not int
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeadDTO {
        private Long id;
        private String fullName;
        private String email;
    }
}