package com.example.lms.enrollment.mapper;

import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.model.Enrollment;
import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;

public class EnrollmentMapper {

    // Convert DTO to Entity
    public static Enrollment toEntity(EnrollmentDTO dto, User student, Course course) {
        return Enrollment.builder()
                .student(student)
                .course(course)
                .status(dto.getStatus())
                .enrollmentDate(dto.getEnrollmentDate())
                .progress(dto.getProgress()) // If you are tracking progress
                .build();
    }

    // Convert Entity to DTO
    public static EnrollmentDTO toDTO(Enrollment enrollment) {
        return EnrollmentDTO.builder()
                .studentId(enrollment.getStudent().getId())
                .courseId(enrollment.getCourse().getId())
                .status(enrollment.getStatus())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .progress(enrollment.getProgress()) // Ensure this field exists in `Enrollment`
                .build();
    }
}
