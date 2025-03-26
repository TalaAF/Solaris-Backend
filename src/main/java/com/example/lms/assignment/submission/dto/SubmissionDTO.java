package com.example.lms.assignment.submission.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionDTO {
    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String filePath;
    private LocalDateTime submissionDate;
    private String feedback;
    private Integer grade;
}