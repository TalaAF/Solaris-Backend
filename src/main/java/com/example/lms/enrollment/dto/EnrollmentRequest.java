package com.example.lms.enrollment.dto;

import lombok.Data;

@Data
public class EnrollmentRequest {
    private Long userId;
    private Long courseId;
}