package com.example.lms.report.service;

import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.progress.repository.ProgressRepository;
import com.example.lms.report.model.CompletionReport;
import com.example.lms.report.repository.CompletionReportRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompletionReportService {

    private final CompletionReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ProgressRepository progressRepository;

    @Transactional
    public CompletionReport generateReport(Long studentId, Long courseId, double progress, boolean isCompleted) {
        // Validate student and course exist
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Additional validation and insights
        validateProgress(progress);

        // Create completion report
        CompletionReport report = new CompletionReport(studentId, courseId, progress, isCompleted);
        
        // Enrich with additional data
        enrichReportWithInsights(report);

        return reportRepository.save(report);
    }

    @Transactional
    public List<CompletionReport> generateBatchReports(Long courseId) {
        // Find all students enrolled in the course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        return course.getStudents().stream()
                .map(student -> {
                    double progress = progressRepository
                        .findByStudentIdAndCourseId(student.getId(), courseId)
                        .map(p -> p.getProgress())
                        .orElse(0.0);

                    boolean isCompleted = progress >= 100.0;
                    return generateReport(student.getId(), courseId, progress, isCompleted);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompletionReport> getReportsForStudent(Long studentId) {
        return reportRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public CompletionReport getLatestReport(Long studentId, Long courseId) {
        return reportRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("No report found"));
    }

    private void validateProgress(double progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
    }

    private void enrichReportWithInsights(CompletionReport report) {
        // Add potential insights or metadata to the report
        report.setAdditionalMetrics(generateAdditionalMetrics(report));
    }

    private String generateAdditionalMetrics(CompletionReport report) {
        // Example of generating additional insights
        return String.format(
            "Total Completed Courses: %d, Average Progress: %.2f%%", 
            progressRepository.countCompletedCourses(report.getStudentId()),
            progressRepository.calculateAverageProgressForStudent(report.getStudentId())
        );
    }
}