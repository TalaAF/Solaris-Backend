package com.example.lms.report.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
public class CompletionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long courseId;
    private double progress;
    private boolean isCompleted;
    private LocalDateTime reportGeneratedAt;

    public CompletionReport() {}

    public CompletionReport(Long studentId, Long courseId, double progress, boolean isCompleted) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.progress = progress;
        this.isCompleted = isCompleted;
        this.reportGeneratedAt = LocalDateTime.now();
    }

   
}