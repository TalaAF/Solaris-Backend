package com.example.lms.notification.repository;

import com.example.lms.notification.model.NotificationPreference;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    
    List<NotificationPreference> findByUser(User user);
    
    Optional<NotificationPreference> findByUserAndType(User user, NotificationType type);
    
    boolean existsByUserAndType(User user, NotificationType type);
}