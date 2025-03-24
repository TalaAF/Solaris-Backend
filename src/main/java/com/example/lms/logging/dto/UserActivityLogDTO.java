package com.example.lms.logging.dto;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class UserActivityLogDTO {
    private Long id;
    private Long userId;
    private String action;
    private String description;
    private LocalDateTime timestamp;

    // Constructor
    public UserActivityLogDTO(Long id, Long userId, String action, String description, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;
    }
}