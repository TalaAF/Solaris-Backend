package com.example.lms.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseDTO {
    private Long id;
    private String code;        // Course code (e.g., SWER402)
    private String name;        // Course name
    private Integer credits;    // Course credits
    private String category;    // Major Requirement, Major Elective, Medical Requirement, etc.
    private String term;        // e.g., Spring 2025
    private String status;      // In Progress, Completed, etc.
    private CourseType type;    // REGISTERED, COMPLETED, AVAILABLE
    
    public enum CourseType {
        REGISTERED,
        COMPLETED,
        AVAILABLE
    }
}