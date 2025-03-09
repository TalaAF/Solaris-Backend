package com.example.lms.report.dto;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class CompletionReportDTO {
    private Long studentId;
    private Long courseId;
    private double progress;
    private boolean isCompleted;
    private LocalDateTime reportGeneratedAt;

    public CompletionReportDTO(Long studentId, Long courseId, double progress, boolean isCompleted, LocalDateTime reportGeneratedAt) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.progress = progress;
        this.isCompleted = isCompleted;
        this.reportGeneratedAt = reportGeneratedAt;
    }

}