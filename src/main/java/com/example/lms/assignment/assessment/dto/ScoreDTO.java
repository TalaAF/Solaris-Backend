package com.example.lms.assignment.assessment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScoreDTO {
    private Long id;
    private Long studentId;
    private Long assignmentId;
    private Integer score;
    private LocalDateTime gradedDate;
}