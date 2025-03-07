package com.example.lms.notification.repository;

import com.example.lms.notification.model.CompletionNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompletionNotificationRepository extends JpaRepository<CompletionNotification, Long> {
    List<CompletionNotification> findByStudentId(Long studentId);
}
