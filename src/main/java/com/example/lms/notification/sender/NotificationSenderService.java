package com.example.lms.notification.sender;

import com.example.lms.notification.model.Notification;
import com.example.lms.notification.model.NotificationPreference;
import com.example.lms.notification.repository.NotificationRepository;
import com.example.lms.notification.service.EmailService;
import com.example.lms.notification.service.NotificationPreferenceService;
import com.example.lms.notification.service.NotificationTemplateService;
import com.example.lms.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSenderService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateService templateService;
    private final NotificationPreferenceService preferenceService;
    private final EmailService emailService;
    

    private final int defaultBatchSize = 25;     
    /**
     * Process unsent in-app notifications
     * Runs every minute
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processUnsentNotifications() {
        Pageable pageable = PageRequest.of(0, defaultBatchSize);
        List<Notification> unsent = notificationRepository.findBySentFalseOrderByPriorityDescCreatedAtAsc(pageable);
        
        for (Notification notification : unsent) {
            try {
                // Mark as sent (in-app delivery)
                notification.setSent(true);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
                
                log.debug("Processed in-app notification ID: {}", notification.getId());
            } catch (Exception e) {
                log.error("Error processing notification ID: " + notification.getId(), e);
            }
        }
    }
    
    /**
     * Process unsent email notifications
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void processUnsentEmailNotifications() {
        Pageable pageable = PageRequest.of(0, defaultBatchSize);
        List<Notification> unsentEmails = notificationRepository
                .findByEmailSentFalseAndSentTrueOrderByPriorityDescCreatedAtAsc(pageable);
        
        for (Notification notification : unsentEmails) {
            try {
                User user = notification.getUser();
                NotificationPreference preference = 
                        preferenceService.getOrCreatePreference(user.getId(), notification.getType());
                
                // Only send email if user has enabled email notifications for this type
                if (preference.isEmailEnabled()) {
                    // Create email data
                    Map<String, Object> emailData = new HashMap<>();
                    emailData.put("userName", user.getFullName());
                    emailData.put("notificationTitle", notification.getTitle());
                    emailData.put("notificationContent", notification.getContent());
                    emailData.put("notificationType", notification.getType().getDisplayName());
                    
                    // Process email content with template
                    String emailSubject = templateService.processSubject(notification.getType(), emailData);
                    String emailContent = templateService.processTemplate(
                            notification.getType(), emailData, "email");
                    
                    // Send email
                    emailService.sendHtmlEmail(user.getEmail(), emailSubject, emailContent);
                    
                    // Mark as email sent
                    notification.setEmailSent(true);
                    notification.setEmailSentAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                    
                    log.debug("Sent email notification ID: {} to user: {}", 
                            notification.getId(), user.getId());
                } else {
                    // If email is disabled, mark as sent anyway
                    notification.setEmailSent(true);
                    notificationRepository.save(notification);
                }
            } catch (Exception e) {
                log.error("Error sending email notification ID: " + notification.getId(), e);
            }
        }
    }
}