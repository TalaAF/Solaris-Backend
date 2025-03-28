package com.example.lms.enrollment.dto;

import com.example.lms.enrollment.model.EnrollmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private EnrollmentStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enrollmentDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessedDate;
    
    // Progress tracking
    private Double progress;
    
    // Additional information for UI display
    private Boolean hasPrerequisites;
    private Boolean isCourseActive;
}