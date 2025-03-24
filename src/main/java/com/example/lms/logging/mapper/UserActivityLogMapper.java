package com.example.lms.logging.mapper;

import com.example.lms.logging.dto.UserActivityLogDTO;
import com.example.lms.logging.model.UserActivityLog;
import org.springframework.stereotype.Component;

@Component
public class UserActivityLogMapper {
    public UserActivityLogDTO toDTO(UserActivityLog log) {
        return new UserActivityLogDTO(
            log.getId(),
            log.getUser().getId(),
            log.getAction(),
            log.getDescription(),
            log.getTimestamp()
        );
    }
}
