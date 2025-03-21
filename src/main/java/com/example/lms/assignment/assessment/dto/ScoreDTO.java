package com.example.lms.assignment.assessment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScoreDTO {
    private Long id;
    private Long studentId;
    private Long assessmentId;
    private Integer score;
    private LocalDateTime gradedDate;
    private String assessmentType;
}