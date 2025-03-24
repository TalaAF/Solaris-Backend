package com.example.lms.assignment.dashboard.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpcomingDeadline {
    private Long assessmentId;
    private String assessmentType;
    private String title;
    private LocalDateTime dueDate;
}