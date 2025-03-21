package com.example.lms.assignment.assessment.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "scores")
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long assessmentId;

    @Column(nullable = false)
    private Integer score;

    @Column
    private LocalDateTime gradedDate;

    @Column(nullable = false)
    private String assessmentType;
}