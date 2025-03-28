package com.example.lms.enrollment.mapper;

import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.model.Enrollment;
import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;
import java.time.LocalDateTime;

public class EnrollmentMapper {

    // Convert Entity to DTO
    public static EnrollmentDTO toDTO(Enrollment enrollment) {
        EnrollmentDTO.EnrollmentDTOBuilder builder = EnrollmentDTO.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getFullName())
                .courseId(enrollment.getCourse().getId())
                .courseName(enrollment.getCourse().getTitle())
                .status(enrollment.getStatus())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .completionDate(enrollment.getCompletionDate())
                .lastAccessedDate(enrollment.getLastAccessedDate())
                .progress(enrollment.getProgress());
        
        // Check if the course is active
        builder.isCourseActive(enrollment.getCourse().isActive());
        
        return builder.build();
    }

    // Convert DTO to Entity
    public static Enrollment toEntity(EnrollmentDTO dto, User student, Course course) {
        return Enrollment.builder()
                .student(student)
                .course(course)
                .status(dto.getStatus())
                .enrollmentDate(LocalDateTime.now())
                .progress(dto.getProgress() != null ? dto.getProgress() : 0.0)
                .build();
    }
    
    // Convert from Entity to minimal DTO (for list views)
    public static EnrollmentDTO toMinimalDTO(Enrollment enrollment) {
        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getFullName())
                .courseId(enrollment.getCourse().getId())
                .courseName(enrollment.getCourse().getTitle())
                .status(enrollment.getStatus())
                .progress(enrollment.getProgress())
                .build();
    }
}