package com.example.lms.report.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionReportDTO {
    private Long studentId;
    private Long courseId;
    private double progress;
    private boolean isCompleted;
    private LocalDateTime reportGeneratedAt;
    private String additionalMetrics;
    
    // Add derived/computed fields
    private transient double averageProgress;
    private transient int completedCoursesCount;
    private transient String courseName;
    private transient String performanceStatus;

    // Helper method to determine performance status
    public String determinePerformanceStatus() {
        if (progress <= 25) {
            return "Needs Improvement";
        } else if (progress <= 50) {
            return "Developing";
        } else if (progress <= 75) {
            return "Good Progress";
        } else if (progress < 100) {
            return "Almost Complete";
        } else {
            return "Completed";
        }
    }

    // Builder method to set additional computed properties
    public static class CompletionReportDTOBuilder {
        public CompletionReportDTOBuilder computeAdditionalProperties(
                double averageProgress, 
                int completedCoursesCount, 
                String courseName, 
                double progress) {
            this.averageProgress = averageProgress;
            this.completedCoursesCount = completedCoursesCount;
            this.courseName = courseName;
            this.performanceStatus = determinePerformanceStatus(progress);
            return this;
        }

        private String determinePerformanceStatus(double progress) {
            if (progress <= 25) {
                return "Needs Improvement";
            } else if (progress <= 50) {
                return "Developing";
            } else if (progress <= 75) {
                return "Good Progress";
            } else if (progress < 100) {
                return "Almost Complete";
            } else {
                return "Completed";
            }
        }
    }
}