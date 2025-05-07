package com.example.lms.notification.factory;

import com.example.lms.notification.model.Notification;
import com.example.lms.notification.model.NotificationPreference;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.notification.repository.NotificationRepository;
import com.example.lms.notification.service.NotificationPreferenceService;
import com.example.lms.notification.service.NotificationTemplateService;
import com.example.lms.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationFactory {
   
   private final NotificationRepository notificationRepository;
   private final NotificationTemplateService templateService;
   private final NotificationPreferenceService preferenceService;
   
   @Transactional
   public Notification createNotification(NotificationType type, User user, String title, String content,
                                        Long relatedEntityId, String relatedEntityType, Map<String, Object> data) {
       // Check user notification preferences
       NotificationPreference preference = preferenceService.getOrCreatePreference(user.getId(), type);
       
       // If both email and in-app notifications are disabled, don't create notification
       if (!preference.isEmailEnabled() && !preference.isInAppEnabled()) {
           log.info("Notification not created for user {} - all channels disabled for type {}", user.getId(), type);
           return null;
       }

       // Check for similar existing notifications to avoid duplication
       List<Notification> similar = notificationRepository.findSimilarNotifications(
               user, type, relatedEntityId, relatedEntityType);
       
       // If similar notification exists and is recent (< 1 hour old), don't create new one
       if (!similar.isEmpty() && similar.stream()
               .anyMatch(n -> n.getCreatedAt().isAfter(LocalDateTime.now().minusHours(1)))) {
           log.info("Similar recent notification exists for user {} and type {}", user.getId(), type);
           return similar.get(0);
       }

       // Process content with templates if needed
       String processedContent = content;
       String processedTitle = title;
       
       if (data != null && !data.isEmpty()) {
           String templateContent = templateService.processTemplate(type, data, "inApp");
           if (templateContent != null) {
               processedContent = templateContent;
           }
           
           String templateTitle = templateService.processSubject(type, data);
           if (templateTitle != null) {
               processedTitle = templateTitle;
           }
       }

       // Create the notification
       Notification notification = Notification.builder()
            .type(type)
            .user(user)
            .title(processedTitle)
            .content(processedContent)
            .relatedEntityId(relatedEntityId)
            .relatedEntityType(relatedEntityType)
            .read(false)
            .sent(false)
            .emailSent(!preference.isEmailEnabled()) 
            .priority(getPriorityForType(type))
            .category(type.getCategory()) // Set the category from the type
            .build();
            
       Notification savedNotification = notificationRepository.save(notification);
       log.debug("Created notification ID: {} for user: {}, type: {}", 
               savedNotification.getId(), user.getId(), type);
       
       return savedNotification;
   }
   
   @Transactional
   public List<Notification> createNotificationsForUsers(NotificationType type, List<User> users,
                                                       String title, String content,
                                                       Long relatedEntityId, String relatedEntityType,
                                                       Map<String, Object> data) {
       List<Notification> notifications = new ArrayList<>();
       
       for (User user : users) {
           Notification notification = createNotification(
                   type, user, title, content, relatedEntityId, relatedEntityType, data);
           if (notification != null) {
               notifications.add(notification);
           }
       }
       
       return notifications;
   }
   
   /**
    * Get priority level for notification type
    */
   private int getPriorityForType(NotificationType type) {
       switch (type) {
           case ASSIGNMENT_DEADLINE_1H:
               return 5; // Highest priority
           case ASSIGNMENT_DEADLINE_12H:
               return 4;
           case ASSIGNMENT_DEADLINE_24H:
               return 3;
           case QUIZ_AVAILABLE:
           case GRADE_POSTED:
               return 2;
           case COURSE_ANNOUNCEMENT:
           case FORUM_MENTION:
               return 2;
           case COURSE_CONTENT_UPLOAD:
           case FORUM_REPLY:
           default:
               return 1; // Lowest priority
       }
   }
}