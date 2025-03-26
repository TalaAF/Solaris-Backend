package com.example.lms.notification.repository;

import com.example.lms.notification.model.NotificationTemplate;
import com.example.lms.notification.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    
    Optional<NotificationTemplate> findByTypeAndActiveTrue(NotificationType type);
}