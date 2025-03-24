package com.example.lms.assignment.dashboard.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpcomingDeadlineDTO {
    private Long assessmentId;
    private String assessmentType;
    private String title;
    private LocalDateTime dueDate;
}