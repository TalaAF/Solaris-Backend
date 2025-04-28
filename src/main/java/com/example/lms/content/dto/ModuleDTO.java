package com.example.lms.content.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.lms.content.model.ModuleStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Module entities.
 * Provides a simplified representation of Module data for API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDTO {
    private Long id;
    private String title;
    private String description;
    private Integer sequence;
    private String status;
    private Long courseId;
    private String courseName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime releaseDate;
    private Boolean isReleased;
    private List<ContentDTO> contents = new ArrayList<>();
    
    /**
     * Check if the module is currently available to students
     * 
     * @return true if available, false otherwise
     */
    public boolean isAvailable() {
        if (!"PUBLISHED".equals(status)) {
            return false;
        }
        
        if (Boolean.FALSE.equals(isReleased)) {
            return false;
        }
        
        if (releaseDate != null && LocalDateTime.now().isBefore(releaseDate)) {
            return false;
        }
        
        return true;
    }
}