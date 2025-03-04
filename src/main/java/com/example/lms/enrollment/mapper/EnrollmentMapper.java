package com.example.lms.enrollment.mapper;

import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.model.Enrollment;

public class EnrollmentMapper {

    public static EnrollmentDTO toDTO(Enrollment enrollment) {
        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .courseId(enrollment.getCourse().getId())
                .status(enrollment.getStatus())
                .enrollmentDate(enrollment.getEnrolledAt())
                .build();
    }

    public static Enrollment toEntity(EnrollmentDTO dto) {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(dto.getId());
        enrollment.setStatus(dto.getStatus());
        enrollment.setEnrolledAt(dto.getEnrollmentDate());
        return enrollment;
    }
}
