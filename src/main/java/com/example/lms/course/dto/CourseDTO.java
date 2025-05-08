package com.example.lms.course.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.hateoas.RepresentationModel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

// Add import for ModuleDTO
import com.example.lms.content.dto.ModuleDTO;

/**
 * Data Transfer Object for Course entities.
 * Contains all fields needed for API responses and requests.
 * Extends RepresentationModel to support HATEOAS.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
// Add callSuper=false to fix the equals/hashCode issue with RepresentationModel
@EqualsAndHashCode(callSuper = false)
public class CourseDTO extends RepresentationModel<CourseDTO> {
    // Basic fields
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;
    
    @NotBlank(message = "Instructor email is required")
    private String instructorEmail; 
    
    // Department information
    private Long departmentId;
    private String departmentName;
    
    // Course capacity and enrollment
    @Min(value = 1, message = "Maximum capacity must be at least 1")
    private Integer maxCapacity;
    private Integer currentEnrollment;
    
    // Prerequisites
    private Set<Long> prerequisiteCourseIds = new HashSet<>();
    
    // Status flags
    private boolean isPublished = true;
    private boolean isArchived = false;
    
    // Dates
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Statistics and metadata
    private Integer contentCount;
    private Integer quizCount;
    private Double averageRating;
    private Integer enrollmentCount;
    
    // Added fields for frontend compatibility
    private String code; // Course code
    private Long instructorId; // Instructor ID
    private String instructorName; // Instructor name
    private Integer progress = 0; // Student progress in course (default 0)
    private List<ModuleDTO> modules; // List of modules in the course
    
    // New field for semester name
    private String semesterName;
    
    // New field for credits
    private Integer credits;
    
    /**
     * Minimal constructor with essential fields
     */
    public CourseDTO(Long id, String title, String description, String instructorEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructorEmail = instructorEmail;
    }
    
    /**
     * Check if course has capacity for more students
     * 
     * @return true if course has capacity, false if it's at max capacity
     */
    public boolean hasCapacity() {
        if (maxCapacity == null) {
            return true; // No capacity limit
        }
        return currentEnrollment == null || currentEnrollment < maxCapacity;
    }
    
    /**
     * Get remaining capacity
     * 
     * @return number of remaining slots, or null if no capacity limit
     */
    public Integer getRemainingCapacity() {
        if (maxCapacity == null) {
            return null;
        }
        return maxCapacity - (currentEnrollment != null ? currentEnrollment : 0);
    }
}