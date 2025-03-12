package com.example.lms.logging.service;

import com.example.lms.logging.model.UserActivityLog;
import com.example.lms.logging.repository.UserActivityLogRepository;
import com.example.lms.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserActivityLogService {

    @Autowired
    private UserActivityLogRepository logRepository;

    public void logActivity(User user, String action, String description) {
        UserActivityLog log = new UserActivityLog();
        log.setUser(user);
        log.setAction(action);
        log.setDescription(description);
        logRepository.save(log);
    }

    public List<UserActivityLog> getUserLogs(Long userId) {
        return logRepository.findByUserId(userId);
    }
}
