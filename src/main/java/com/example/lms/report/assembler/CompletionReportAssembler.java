package com.example.lms.report.assembler;

import com.example.lms.course.repository.CourseRepository;
import com.example.lms.progress.repository.ProgressRepository;
import com.example.lms.report.dto.CompletionReportDTO;
import com.example.lms.report.model.CompletionReport;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CompletionReportAssembler {
    
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
            .performanceCategory(report.getPerformanceCategory().name())
            .build();

        // Enrich DTO with additional computed properties
        enrichDTO(dto, report);

        return dto;
    }

    private void enrichDTO(CompletionReportDTO dto, CompletionReport report) {
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

        // Get student name
        String studentName = userRepository.findById(dto.getStudentId())
            .map(user -> user.getFullName())
            .orElse("Unknown Student");

        // Set computed properties
        dto.setAverageProgress(averageProgress != null ? averageProgress : 0.0);
        dto.setCompletedCoursesCount(completedCoursesCount != null ? completedCoursesCount.intValue() : 0);
        dto.setCourseName(courseName);
        dto.setStudentName(studentName);

        // Add metadata from the original report
        dto.setMetadata(convertMetadataToMap(report));
    }

    public CompletionReport toEntity(CompletionReportDTO dto) {
        CompletionReport report = CompletionReport.builder()
            .studentId(dto.getStudentId())
            .courseId(dto.getCourseId())
            .progress(dto.getProgress())
            .isCompleted(dto.isCompleted())
            .reportGeneratedAt(dto.getReportGeneratedAt())
            .build();
        
        // Set performance category
        report.setPerformanceCategory(
            CompletionReport.PerformanceCategory.valueOf(dto.getPerformanceCategory())
        );
        
        // Set additional metrics
        report.setAdditionalMetrics(generateAdditionalMetrics(dto));
        
        // Add metadata from DTO
        if (dto.getMetadata() != null) {
            dto.getMetadata().forEach(report::addMetadata);
        }
        
        return report;
    }

    private String generateAdditionalMetrics(CompletionReportDTO dto) {
        return String.format(
            "Course: %s, Student: %s, Avg Progress: %.2f%%, Completed Courses: %d, Status: %s", 
            dto.getCourseName(),
            dto.getStudentName(),
            dto.getAverageProgress(),
            dto.getCompletedCoursesCount(),
            dto.getPerformanceCategory()
        );
    }

    private Map<String, String> convertMetadataToMap(CompletionReport report) {
        // Convert the report's metadata to a map, or return an empty map if null
        return report.getMetadata() != null 
            ? new HashMap<>(report.getMetadata()) 
            : new HashMap<>();
    }
}