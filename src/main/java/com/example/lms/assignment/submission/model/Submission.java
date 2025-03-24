package com.example.lms.assignment.submission.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "submissions")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long assignmentId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private LocalDateTime submissionDate;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column
    private Integer grade;
}