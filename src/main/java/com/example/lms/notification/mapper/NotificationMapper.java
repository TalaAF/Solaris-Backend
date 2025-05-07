package com.example.lms.notification.mapper;

import com.example.lms.notification.dto.NotificationDTO;
import com.example.lms.notification.dto.NotificationPreferenceDTO;
import com.example.lms.notification.model.Notification;
import com.example.lms.notification.model.NotificationPreference;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    
    /**
     * Convert Notification entity to DTO
     */
    public NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .priority(notification.getPriority())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .category(notification.getType().getCategory()) // Map the category
                .build();
    }
    
    /**
     * Convert NotificationPreference entity to DTO
     */
    public NotificationPreferenceDTO toPreferenceDTO(NotificationPreference preference) {
        return NotificationPreferenceDTO.builder()
                .id(preference.getId())
                .type(preference.getType())
                .typeDisplayName(preference.getType().getDisplayName())
                .emailEnabled(preference.isEmailEnabled())
                .inAppEnabled(preference.isInAppEnabled())
                .build();
    }
}