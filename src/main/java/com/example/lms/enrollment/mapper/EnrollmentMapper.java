package com.example.lms.enrollment.mapper;

import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.model.Enrollment;
import com.example.lms.enrollment.model.EnrollmentStatus; // Add this import
import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EnrollmentMapper {

    // Convert Entity to DTO
    public static EnrollmentDTO toDTO(Enrollment enrollment) {
        // Map EnrollmentStatus to "active"/"inactive" for frontend
        String status = mapStatusForFrontend(enrollment.getStatus());
        
        // Convert grade if you've added this field
        String grade = null; // Replace with actual grade if implemented
        
        // Create user data object for frontend format
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", enrollment.getStudent().getId());
        userData.put("fullName", enrollment.getStudent().getFullName());
        userData.put("email", enrollment.getStudent().getEmail());
        userData.put("roleNames", enrollment.getStudent().getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()));
        
        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getFullName())
                .courseId(enrollment.getCourse().getId())
                .courseName(enrollment.getCourse().getTitle())
                .status(enrollment.getStatus())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .completionDate(enrollment.getCompletionDate())
                .lastAccessedDate(enrollment.getLastAccessedDate())
                .progress(enrollment.getProgress())
                .user(userData) // Add nested user data for frontend
                .build();
    }
    
    private static String mapStatusForFrontend(EnrollmentStatus status) {
        switch (status) {
            case APPROVED:
            case IN_PROGRESS:
                return "active";
            default:
                return "inactive";
        }
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