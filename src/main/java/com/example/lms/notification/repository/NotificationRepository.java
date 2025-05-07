package com.example.lms.notification.repository;

import java.util.List;
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
    
    // Find notifications by user and category
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.category = :category ORDER BY n.createdAt DESC")
    List<Notification> findByUserAndCategory(@Param("user") User user, @Param("category") String category);
    
    // Find unread notifications by user and category
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.category = :category AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findByUserAndCategoryAndReadFalse(@Param("user") User user, @Param("category") String category);
    
    // Count unread notifications by user and category
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.category = :category AND n.read = false")
    long countByUserAndCategoryAndReadFalse(@Param("user") User user, @Param("category") String category);
    
    // Keep all other existing methods
    List<Notification> findByContentContaining(String text);
    
    @Query("SELECT n FROM Notification n WHERE n.content LIKE %:text%")
    List<Notification> findSimilarNotifications(@Param("text") String text);

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    long countByUserAndReadFalse(User user);

    boolean existsById(Long notificationId);

    void deleteById(Long notificationId);

    List<Notification> findBySentFalseOrderByPriorityDescCreatedAtAsc(Pageable pageable);

    List<Notification> findByEmailSentFalseAndSentTrueOrderByPriorityDescCreatedAtAsc(Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.type = :type " +
           "AND n.relatedEntityId = :relatedEntityId AND n.relatedEntityType = :relatedEntityType")
    List<Notification> findSimilarNotifications(
        @Param("user") User user, 
        @Param("type") NotificationType type, 
        @Param("relatedEntityId") Long relatedEntityId,
        @Param("relatedEntityType") String relatedEntityType
    );
}