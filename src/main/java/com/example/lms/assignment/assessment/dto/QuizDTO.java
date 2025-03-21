package com.example.lms.assignment.assessment.dto;

import lombok.Data;

@Data
public class QuizDTO {
    private Long id;
    private Long courseId;
    private String title;
    private String questions;
    private Integer maxScore;
}