package com.example.lms.enrollment.dto;

import com.example.lms.enrollment.model.EnrollmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDTO {
    private Long id;
    private Long studentId;
    private Long courseId;
    private EnrollmentStatus status;
    private LocalDateTime enrollmentDate;
}
