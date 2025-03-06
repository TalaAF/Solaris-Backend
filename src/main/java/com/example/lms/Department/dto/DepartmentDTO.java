package com.example.lms.Department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
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
        
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;
        
        @NotBlank(message = "Department code is required")
        @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "Department code must be 2-10 uppercase letters or numbers")
        private String code;
        
        private String specialtyArea;
        private String headOfDepartment;
        private String contactInformation;
        private boolean isActive = true;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String code;
        private String specialtyArea;
        private String headOfDepartment;
        private String contactInformation;
        private boolean isActive;
        private int userCount;
    }
}