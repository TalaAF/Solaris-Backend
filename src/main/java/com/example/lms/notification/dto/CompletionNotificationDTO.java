package com.example.lms.notification.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CompletionNotificationDTO {
    private Long id;
    private Long studentId;
    private Long courseId;
    private String message;
    private boolean read;
    private LocalDateTime timestamp;
}
