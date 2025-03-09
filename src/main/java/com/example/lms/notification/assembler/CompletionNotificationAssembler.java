package com.example.lms.notification.assembler;

import com.example.lms.notification.dto.CompletionNotificationDTO;
import com.example.lms.notification.model.CompletionNotification;
import com.example.lms.notification.mapper.CompletionNotificationMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CompletionNotificationAssembler {

    public CompletionNotification toEntity(CompletionNotificationDTO dto) {
        return CompletionNotificationMapper.toEntity(dto);
    }

    public CompletionNotificationDTO toDTO(CompletionNotification notification) {
        return CompletionNotificationMapper.toDTO(notification);
    }

    public List<CompletionNotificationDTO> toDTO(List<CompletionNotification> notifications) {
        return notifications.stream().map(CompletionNotificationMapper::toDTO).collect(Collectors.toList());
    }
}
