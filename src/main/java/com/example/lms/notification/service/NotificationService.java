package com.example.lms.notification.service;

import com.example.lms.notification.model.Notification;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.user.model.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    
    /**
     * Create a notification for a user
     * 
     * @param type Type of notification
     * @param user User to receive the notification
     * @param title Title of the notification
     * @param content Content of the notification
     * @param relatedEntityId ID of related entity (optional)
     * @param relatedEntityType Type of related entity (optional)
     * @param data Additional data to use in templates
     * @return The created notification
     */
    Notification createNotification(NotificationType type, User user, String title, String content, 
                                   Long relatedEntityId, String relatedEntityType, Map<String, Object> data);
    
    /**
     * Create notifications for multiple users
     * 
     * @param type Type of notification
     * @param users Users to receive the notification
     * @param title Title of the notification
     * @param content Content of the notification
     * @param relatedEntityId ID of related entity (optional)
     * @param relatedEntityType Type of related entity (optional)
     * @param data Additional data to use in templates
     * @return List of created notifications
     */
    List<Notification> createNotificationsForUsers(NotificationType type, List<User> users, 
                                                  String title, String content, 
                                                  Long relatedEntityId, String relatedEntityType,
                                                  Map<String, Object> data);
    
    /**
     * Mark a notification as read
     * 
     * @param notificationId ID of the notification
     * @return The updated notification
     */
    Notification markAsRead(Long notificationId);
    
    /**
     * Mark all notifications as read for a user
     * 
     * @param userId ID of the user
     */
    void markAllAsRead(Long userId);
    
    /**
     * Get unread notifications for a user
     * 
     * @param userId ID of the user
     * @return List of unread notifications
     */
    List<Notification> getUnreadNotifications(Long userId);
    
    /**
     * Get all notifications for a user (paginated)
     * 
     * @param userId ID of the user
     * @param page Page number
     * @param size Page size
     * @return Page of notifications
     */
    Page<Notification> getAllNotifications(Long userId, int page, int size);
    
    /**
     * Get unread notification count for a user
     * 
     * @param userId ID of the user
     * @return Count of unread notifications
     */
    long getUnreadNotificationCount(Long userId);
    
    /**
     * Delete a notification
     * 
     * @param notificationId ID of the notification
     */
    void deleteNotification(Long notificationId);
    
    /**
     * Process unsent notifications (for batch processing)
     * 
     * @param batchSize Maximum number of notifications to process
     */
    void processUnsentNotifications(int batchSize);
    
    /**
     * Process unsent email notifications (for batch processing)
     * 
     * @param batchSize Maximum number of email notifications to process
     */
    void processUnsentEmailNotifications(int batchSize);
    
    /**
     * Create course content upload notification
     */
    void notifyCourseContentUpload(Long courseId, String contentTitle, List<Long> studentIds);
    
    /**
     * Create assignment deadline reminder notifications
     */
    void notifyAssignmentDeadline(Long assignmentId, int hoursRemaining);
    
    /**
     * Create quiz availability notification
     */
    void notifyQuizAvailable(Long quizId, List<Long> studentIds);
    
    /**
     * Create grade posting notification
     */
    void notifyGradePosted(Long courseId, Long studentId, String assessmentType, String assessmentTitle);
    
    /**
     * Create course announcement notification
     */
    void notifyCourseAnnouncement(Long courseId, String announcementTitle, String announcementContent);
    
    /**
     * Create forum reply notification
     */
    void notifyForumReply(Long forumPostId, Long replyId, Long authorId);
    
    /**
     * Create forum mention notification
     */
    void notifyForumMention(Long forumPostId, Long mentionedUserId, Long authorId);
}