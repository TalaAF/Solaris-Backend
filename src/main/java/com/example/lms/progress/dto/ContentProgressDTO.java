package com.example.lms.progress.dto;

import java.time.LocalDateTime;

import com.example.lms.user.model.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentProgressDTO {
    private Long id;
    private Long contentId;
    private Long studentId;
    private Double progress; // 0-100%
    private boolean completed;
    private LocalDateTime lastUpdated; 
}
