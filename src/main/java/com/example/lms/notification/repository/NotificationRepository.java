package com.example.lms.notification.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.lms.notification.model.Notification;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.user.model.User;

public interface NotificationRepository {

    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);

    void saveAll(List<Notification> unreadNotifications);

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    long countByUserAndReadFalse(User user);

    boolean existsById(Long notificationId);

    void deleteById(Long notificationId);

    List<Notification> findBySentFalseOrderByPriorityDescCreatedAtAsc(Pageable pageable);

    Notification save(Notification notification);

    List<Notification> findByEmailSentFalseAndSentTrueOrderByPriorityDescCreatedAtAsc(Pageable pageable);

    Notification findById(Long notificationId);

    List<Notification> findSimilarNotifications(User user, NotificationType type, Long relatedEntityId,
            String relatedEntityType);

    

   
}
