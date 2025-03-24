package com.example.lms.assignment.forum.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumPostDTO {
    private Long id;
    private Long threadId;
    private Long userId;
    private String content;
    private LocalDateTime postedDate;
}