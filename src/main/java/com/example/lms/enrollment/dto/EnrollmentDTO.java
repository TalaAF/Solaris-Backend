package com.example.lms.enrollment.dto;

import com.example.lms.enrollment.model.EnrollmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentDTO {

    private Long studentId;
    private Long courseId;
    private EnrollmentStatus status;
    private LocalDateTime enrollmentDate;

    // Add progress field
    private Double progress;
}
