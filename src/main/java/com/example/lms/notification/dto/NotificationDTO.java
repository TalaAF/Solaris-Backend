package com.example.lms.notification.dto;

import com.example.lms.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private NotificationType type;
    private String title;
    private String content;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private int priority;
    private String relatedEntityType;
    private Long relatedEntityId;
}