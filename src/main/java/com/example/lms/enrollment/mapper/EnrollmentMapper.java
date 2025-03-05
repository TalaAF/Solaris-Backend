package com.example.lms.enrollment.mapper;

import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.model.Enrollment;
import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;

public class EnrollmentMapper {

    public static Enrollment toEntity(EnrollmentDTO dto, User student, Course course) {
        if (dto == null) {
            return null;
        }

        return Enrollment.builder()
                .student(student) // Assign student entity
                .course(course)   // Assign course entity
                .status(dto.getStatus())
                .enrollmentDate(dto.getEnrollmentDate())
                .progress(dto.getProgress() != null ? dto.getProgress() : 0.0) // Handle possible null progress
                .build();
    }

    public static EnrollmentDTO toDTO(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }

        return EnrollmentDTO.builder()
                .studentId(enrollment.getStudent().getId()) // Convert student entity to studentId
                .courseId(enrollment.getCourse().getId())   // Convert course entity to courseId
                .status(enrollment.getStatus())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .progress(enrollment.getProgress())
                .build();
    }
}
