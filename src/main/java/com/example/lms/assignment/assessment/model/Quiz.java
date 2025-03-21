package com.example.lms.assignment.assessment.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String questions;

    @Column(nullable = false)
    private Integer maxScore;
}