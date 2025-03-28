package com.example.lms.notification.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.lms.notification.model.Notification;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);
    List<Notification> findByContentContaining(String text);
    
       @Query("SELECT n FROM Notification n WHERE n.content LIKE %:text%")
    List<Notification> findSimilarNotifications(@Param("text") String text);

    void saveAll(List<Notification> unreadNotifications);

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    long countByUserAndReadFalse(User user);

    boolean existsById(Long notificationId);

    void deleteById(Long notificationId);

    List<Notification> findBySentFalseOrderByPriorityDescCreatedAtAsc(Pageable pageable);

    Notification save(Notification notification);

    List<Notification> findByEmailSentFalseAndSentTrueOrderByPriorityDescCreatedAtAsc(Pageable pageable);


    List<Notification> findSimilarNotifications(User user, NotificationType type, Long relatedEntityId,
                                                String relatedEntityType);


}
