package com.example.lms.assignment.dashboard.dto;

import lombok.Data;

@Data
public class CourseProgressDTO {
    private Long courseId;
    private String courseName;
    private Double completionPercentage;
}