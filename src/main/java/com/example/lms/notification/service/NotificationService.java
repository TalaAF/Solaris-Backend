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
     */
    Notification createNotification(NotificationType type, User user, String title, String content,
                                  Long relatedEntityId, String relatedEntityType, Map<String, Object> data);
    
    /**
     * Create notifications for multiple users
     */
    List<Notification> createNotificationsForUsers(NotificationType type, List<User> users,
                                                 String title, String content,
                                                 Long relatedEntityId, String relatedEntityType,
                                                 Map<String, Object> data);
    
    /**
     * Mark a notification as read
     */
    Notification markAsRead(Long notificationId);
    
    /**
     * Mark all notifications as read for a user
     */
    void markAllAsRead(Long userId);
    
    /**
     * Get unread notifications for a user
     */
    List<Notification> getUnreadNotifications(Long userId);
    
    /**
     * Get all notifications for a user with pagination
     */
    Page<Notification> getAllNotifications(Long userId, int page, int size);
    
    /**
     * Get count of unread notifications for a user
     */
    long getUnreadNotificationCount(Long userId);
    
    /**
     * Delete a notification
     */
    void deleteNotification(Long notificationId);
    /**
 * Get notifications by category
 */
List<Notification> getNotificationsByCategory(Long userId, String category);

/**
 * Get count of unread notifications by category
 */
long getUnreadNotificationCountByCategory(Long userId, String category);

/**
 * Mark all notifications as read for a user in a specific category
 */
void markAllAsReadInCategory(Long userId, String category);
}