package com.example.lms.notification.service;

import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.notification.model.NotificationTemplate;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTemplateService {
    
    private final NotificationTemplateRepository templateRepository;
    private final TemplateEngine templateEngine;
    
    // Template cache for better performance
    private final Map<NotificationType, NotificationTemplate> templateCache = new ConcurrentHashMap<>();
    
    /**
     * Get template by notification type with caching
     */
    @Transactional(readOnly = true)
    public NotificationTemplate getTemplateByType(NotificationType type) {
        return templateCache.computeIfAbsent(type, t -> {
            try {
                return templateRepository.findByTypeAndActiveTrue(t)
                    .orElseThrow(() -> new ResourceNotFoundException("Template not found for type: " + t));
            } catch (ResourceNotFoundException e) {
                log.warn("Template not found for type: {}. Using default handling.", type);
                return null;
            }
        });
    }
    
    /**
     * Process template with placeholders
     */
    public String processTemplate(NotificationType type, Map<String, Object> placeholders, String templateType) {
        try {
            NotificationTemplate template = getTemplateByType(type);
            
            if (template == null) {
                return null;
            }
            
            if ("email".equalsIgnoreCase(templateType)) {
                // Use Thymeleaf for email templates
                Context context = new Context(Locale.getDefault());
                
                // Add all placeholders to the context
                placeholders.forEach(context::setVariable);
                
                // Process the template with Thymeleaf
                return templateEngine.process("email/" + template.getEmailTemplateName(), context);
            } else {
                // For in-app notifications, use the stored template string
                String templateContent = template.getInAppTemplate();
                return replacePlaceholders(templateContent, placeholders);
            }
        } catch (Exception e) {
            log.error("Error processing template for type: " + type, e);
            return null;
        }
    }
    
    /**
     * Process subject with placeholders
     */
    public String processSubject(NotificationType type, Map<String, Object> placeholders) {
        try {
            NotificationTemplate template = getTemplateByType(type);
            
            if (template == null) {
                return null;
            }
            
            return replacePlaceholders(template.getSubject(), placeholders);
        } catch (Exception e) {
            log.error("Error processing subject for type: " + type, e);
            return null;
        }
    }
    
    /**
     * Replace placeholders in a template
     */
    private String replacePlaceholders(String template, Map<String, Object> placeholders) {
        String processed = template;
        
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            processed = processed.replace(placeholder, value);
        }
        
        return processed;
    }
    
    /**
     * Refresh template cache (scheduled or manually triggered)
     */
    @Transactional
    public void refreshTemplateCache() {
        templateCache.clear();
        log.debug("Notification template cache cleared");
    }
    
    /**
     * Initialize default templates
     */
    @PostConstruct
    @Transactional
    public void initializeDefaultTemplates() {
        // Only create templates if none exist
        if (templateRepository.count() == 0) {
            log.info("Initializing default notification templates");
            
            createDefaultTemplate(
                NotificationType.COURSE_CONTENT_UPLOAD,
                "New Content Available: {{contentTitle}}",
                "course-content-upload.html",
                "New content '{{contentTitle}}' has been added to your course {{courseName}} by {{instructorName}}."
            );
            
            createDefaultTemplate(
                NotificationType.ASSIGNMENT_DEADLINE_24H,
                "Assignment Due in 24 Hours: {{assignmentTitle}}",
                "assignment-deadline.html",
                "Your assignment '{{assignmentTitle}}' for course {{courseName}} is due in 24 hours ({{dueDate}})."
            );
            
            createDefaultTemplate(
                NotificationType.ASSIGNMENT_DEADLINE_12H,
                "Assignment Due in 12 Hours: {{assignmentTitle}}",
                "assignment-deadline.html",
                "Your assignment '{{assignmentTitle}}' for course {{courseName}} is due in 12 hours ({{dueDate}})."
            );
            
            createDefaultTemplate(
                NotificationType.ASSIGNMENT_DEADLINE_1H,
                "URGENT: Assignment Due in 1 Hour: {{assignmentTitle}}",
                "assignment-deadline.html",
                "URGENT: Your assignment '{{assignmentTitle}}' for course {{courseName}} is due in 1 hour ({{dueDate}})."
            );
            
            createDefaultTemplate(
                NotificationType.QUIZ_AVAILABLE,
                "New Quiz Available: {{quizTitle}}",
                "quiz-available.html",
                "A new quiz '{{quizTitle}}' is now available in your course {{courseName}}. Start date: {{startDate}}, End date: {{endDate}}, Time limit: {{timeLimit}}."
            );
            
            createDefaultTemplate(
                NotificationType.GRADE_POSTED,
                "Grade Posted: {{assessmentTitle}}",
                "grade-posted.html",
                "Your grade for {{assessmentTitle}} in course {{courseName}} has been posted. Please log in to view your grade."
            );
            
            createDefaultTemplate(
                NotificationType.COURSE_ANNOUNCEMENT,
                "Announcement: {{announcementTitle}}",
                "course-announcement.html",
                "{{announcementTitle}} - {{announcementContent}} (From: {{instructorName}}, Course: {{courseName}})"
            );
            
            createDefaultTemplate(
                NotificationType.FORUM_REPLY,
                "New Reply to Your Forum Post",
                "forum-reply.html",
                "{{replyAuthorName}} replied to your post: \"{{replyContent}}\""
            );
            
            createDefaultTemplate(
                NotificationType.FORUM_MENTION,
                "You Were Mentioned in a Forum Post",
                "forum-mention.html",
                "{{authorName}} mentioned you in a forum post: \"{{postContent}}\""
            );
            
            log.info("Default notification templates initialized");
        }
    }
    
    /**
     * Helper to create default template
     */
    private void createDefaultTemplate(NotificationType type, String subject, 
                                     String emailTemplate, String inAppTemplate) {
        NotificationTemplate template = NotificationTemplate.builder()
                .type(type)
                .subject(subject)
                .emailTemplateName(emailTemplate)
                .inAppTemplate(inAppTemplate)
                .active(true)
                .build();
        
        templateRepository.save(template);
    }
}