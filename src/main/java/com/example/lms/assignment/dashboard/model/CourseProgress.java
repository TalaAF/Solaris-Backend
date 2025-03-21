package com.example.lms.assignment.dashboard.model;

import lombok.Data;

@Data
public class CourseProgress {
    private Long courseId;
    private String courseName;
    private Double completionPercentage;
}