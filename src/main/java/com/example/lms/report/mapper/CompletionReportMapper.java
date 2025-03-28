package com.example.lms.report.mapper;

import com.example.lms.course.repository.CourseRepository;
import com.example.lms.progress.repository.ProgressRepository;
import com.example.lms.report.dto.CompletionReportDTO;
import com.example.lms.report.model.CompletionReport;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompletionReportMapper {
    
    private final CourseRepository courseRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;

    public CompletionReportDTO toDTO(CompletionReport report) {
        // Basic mapping
        CompletionReportDTO dto = CompletionReportDTO.builder()
            .studentId(report.getStudentId())
            .courseId(report.getCourseId())
            .progress(report.getProgress())
            .isCompleted(report.isCompleted())
            .reportGeneratedAt(report.getReportGeneratedAt())
            .additionalMetrics(report.getAdditionalMetrics())
            .build();

        // Enrich DTO with additional computed properties
        enrichDTO(dto);

        return dto;
    }

    private void enrichDTO(CompletionReportDTO dto) {
        // Calculate average progress for the student
        Double averageProgress = progressRepository
            .calculateAverageProgressForStudent(dto.getStudentId());
        
        // Count completed courses
        Long completedCoursesCount = progressRepository
            .countCompletedCourses(dto.getStudentId());
        
        // Get course name
        String courseName = courseRepository.findById(dto.getCourseId())
            .map(course -> course.getTitle())
            .orElse("Unknown Course");

        // Set computed properties
        dto.setAverageProgress(averageProgress != null ? averageProgress : 0.0);
        dto.setCompletedCoursesCount(completedCoursesCount != null ? completedCoursesCount.intValue() : 0);
        dto.setCourseName(courseName);
        dto.setPerformanceStatus(dto.determinePerformanceStatus());
    }

    public CompletionReport toEntity(CompletionReportDTO dto) {
        CompletionReport report = new CompletionReport(
            dto.getStudentId(),
            dto.getCourseId(),
            dto.getProgress(),
            dto.isCompleted()
        );
        
        // Set additional metrics if available
        report.setAdditionalMetrics(generateAdditionalMetrics(dto));
        
        return report;
    }

    private String generateAdditionalMetrics(CompletionReportDTO dto) {
        return String.format(
            "Course: %s, Avg Progress: %.2f%%, Completed Courses: %d, Status: %s", 
            dto.getCourseName(),
            dto.getAverageProgress(),
            dto.getCompletedCoursesCount(),
            dto.getPerformanceStatus()
        );
    }
}