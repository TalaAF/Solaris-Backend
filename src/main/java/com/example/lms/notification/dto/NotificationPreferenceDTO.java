package com.example.lms.notification.dto;

import com.example.lms.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {
    private Long id;
    private NotificationType type;
    private String typeDisplayName;
    private boolean emailEnabled;
    private boolean inAppEnabled;
}