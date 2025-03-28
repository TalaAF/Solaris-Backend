package com.example.lms.report.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Entity
@Table(name = "completion_reports")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private double progress;

    @Column(nullable = false)
    private boolean isCompleted;

    @Column(nullable = false)
    private LocalDateTime reportGeneratedAt;

    @Column(length = 1000)
    private String additionalMetrics;

    // New field to store extended metadata
    @ElementCollection
    @CollectionTable(name = "report_metadata", joinColumns = @JoinColumn(name = "report_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();
    public CompletionReport(Long studentId, Long courseId, double progress, boolean isCompleted) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.progress = progress;
        this.isCompleted = isCompleted;}

    // Enum to categorize performance
    @Enumerated(EnumType.STRING)
    private PerformanceCategory performanceCategory;

    // Performance categories
    public enum PerformanceCategory {
        NEEDS_IMPROVEMENT,
        DEVELOPING,
        GOOD_PROGRESS,
        EXCELLENT,
        COMPLETED
    }

    // Method to determine performance category
    public PerformanceCategory determinePerformanceCategory() {
        if (progress <= 25) {
            return PerformanceCategory.NEEDS_IMPROVEMENT;
        } else if (progress <= 50) {
            return PerformanceCategory.DEVELOPING;
        } else if (progress <= 75) {
            return PerformanceCategory.GOOD_PROGRESS;
        } else if (progress < 100) {
            return PerformanceCategory.EXCELLENT;
        } else {
            return PerformanceCategory.COMPLETED;
        }
    }

    // Convenience method to add metadata
    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    // Lifecycle method to set performance category and generate additional metrics
    @PrePersist
    @PreUpdate
    public void updateReportDetails() {
        // Set performance category
        this.performanceCategory = determinePerformanceCategory();

        // Generate additional metrics if not already set
        if (this.additionalMetrics == null || this.additionalMetrics.isEmpty()) {
            this.additionalMetrics = generateAdditionalMetrics();
        }

        // Ensure report generation timestamp is set
        if (this.reportGeneratedAt == null) {
            this.reportGeneratedAt = LocalDateTime.now();
        }
    }

    // Method to generate additional metrics
    private String generateAdditionalMetrics() {
        return String.format(
            "Progress: %.2f%%, Status: %s, Completed: %b, Category: %s",
            progress, 
            isCompleted ? "Completed" : "In Progress", 
            isCompleted,
            performanceCategory.name()
        );
    }
}