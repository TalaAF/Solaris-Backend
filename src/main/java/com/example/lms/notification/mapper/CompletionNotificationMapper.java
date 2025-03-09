package com.example.lms.notification.mapper;

import com.example.lms.notification.dto.CompletionNotificationDTO;
import com.example.lms.notification.model.CompletionNotification;

public class CompletionNotificationMapper {

    public static CompletionNotificationDTO toDTO(CompletionNotification notification) {
        CompletionNotificationDTO dto = new CompletionNotificationDTO();
        dto.setId(notification.getId());
        dto.setStudentId(notification.getStudent().getId());
        dto.setCourseId(notification.getCourse().getId());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setTimestamp(notification.getTimestamp());
        return dto;
    }

    public static CompletionNotification toEntity(CompletionNotificationDTO dto) {
        CompletionNotification notification = new CompletionNotification();
        notification.setId(dto.getId());
        notification.setMessage(dto.getMessage());
        notification.setRead(dto.isRead());
        notification.setTimestamp(dto.getTimestamp());
        return notification;
    }
}
