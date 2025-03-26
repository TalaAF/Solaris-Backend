package com.example.lms.assignment.assignments.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignmentDTO {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Integer maxScore;
}