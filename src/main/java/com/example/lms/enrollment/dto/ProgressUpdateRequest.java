package com.example.lms.enrollment.dto;

import lombok.Data;

@Data
public class ProgressUpdateRequest {
    private Double progress;
    private String grade;
}