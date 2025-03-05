package com.example.lms.enrollment.mapper;

import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.model.Enrollment;

public class EnrollmentMapper {
    public static EnrollmentDTO toDTO(Enrollment enrollment) {
        return EnrollmentDTO.builder()
                .studentId(enrollment.getStudent().getId())
                .courseId(enrollment.getCourse().getId())
                .status(enrollment.getStatus())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .progress(enrollment.getProgress()) // Include progress
                .build();
    }
}
