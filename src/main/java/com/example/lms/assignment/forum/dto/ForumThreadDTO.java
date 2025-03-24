package com.example.lms.assignment.forum.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumThreadDTO {
    private Long id;
    private Long courseId;
    private String title;
    private Long createdBy;
    private LocalDateTime createdDate;
}