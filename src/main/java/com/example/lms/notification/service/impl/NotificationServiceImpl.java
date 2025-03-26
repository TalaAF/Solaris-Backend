package com.example.lms.notification.service.impl;

import com.example.lms.assignment.assessment.model.Assignment;
import com.example.lms.assignment.assessment.repository.AssignmentRepository;
import com.example.lms.assignment.forum.model.ForumPost;
import com.example.lms.assignment.forum.repository.ForumPostRepository;
import com.example.lms.assessment.model.Quiz;
import com.example.lms.assessment.repository.QuizRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.notification.model.Notification;
import com.example.lms.notification.model.NotificationPreference;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.notification.repository.NotificationRepository;
import com.example.lms.notification.service.EmailService;
import com.example.lms.notification.service.NotificationPreferenceService;
import com.example.lms.notification.service.NotificationService;
import com.example.lms.notification.service.NotificationTemplateService;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final QuizRepository quizRepository;
    private final ForumPostRepository forumPostRepository;
    private final NotificationTemplateService templateService;
    private final NotificationPreferenceService preferenceService;
    private final EmailService emailService;

    @Override
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
        if (data != null && !data.isEmpty()) {
            String processedContent = templateService.processTemplate(type, data, "inApp");
            if (processedContent != null) {
                content = processedContent;
            }
            
            String processedTitle = templateService.processSubject(type, data);
            if (processedTitle != null) {
                title = processedTitle;
            }
        }

        // Create the notification
        Notification notification = Notification.builder()
                .type(type)
                .user(user)
                .title(title)
                .content(content)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .read(false)
                .sent(false)
                .emailSent(!preference.isEmailEnabled()) // Mark as sent if email not enabled
                .priority(getPriorityForType(type))
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        log.debug("Created notification ID: {} for user: {}, type: {}", 
                savedNotification.getId(), user.getId(), type);
        
        return savedNotification;
    }

    @Override
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

    @Override
    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Notification> unreadNotifications = notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
        
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getAllNotifications(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + notificationId);
        }
        
        notificationRepository.deleteById(notificationId);
    }

    @Override
    @Transactional
    public void processUnsentNotifications(int batchSize) {
        Pageable pageable = PageRequest.of(0, batchSize);
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

    @Override
    @Transactional
    public void processUnsentEmailNotifications(int batchSize) {
        Pageable pageable = PageRequest.of(0, batchSize);
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

    @Override
    @Transactional
    @Async
    public void notifyCourseContentUpload(Long courseId, String contentTitle, List<Long> studentIds) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
            
            List<User> students = new ArrayList<>();
            if (studentIds != null && !studentIds.isEmpty()) {
                students = userRepository.findAllById(studentIds);
            } else {
                // If no specific students provided, notify all enrolled students
                students = course.getStudents().stream().collect(Collectors.toList());
            }
            
            if (students.isEmpty()) {
                log.info("No students to notify for course content upload in course ID: {}", courseId);
                return;
            }
            
            String title = "New Content: " + contentTitle;
            String content = "New content has been added to your course: " + course.getTitle();
            
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("contentTitle", contentTitle);
            data.put("instructorName", course.getInstructor().getFullName());
            
            createNotificationsForUsers(
                    NotificationType.COURSE_CONTENT_UPLOAD,
                    students,
                    title,
                    content,
                    courseId,
                    "course",
                    data
            );
            
            log.info("Sent course content upload notifications to {} students for course ID: {}", 
                    students.size(), courseId);
        } catch (Exception e) {
            log.error("Error sending course content upload notifications for course ID: " + courseId, e);
        }
    }

    @Override
    @Transactional
    @Async
    public void notifyAssignmentDeadline(Long assignmentId, int hoursRemaining) {
        try {
            Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
            
            // Get the course and its enrolled students
            Course course = assignment.getCourse();
            List<User> students = course.getStudents().stream().collect(Collectors.toList());
            
            if (students.isEmpty()) {
                log.info("No students to notify for assignment deadline in course ID: {}", course.getId());
                return;
            }
            
            NotificationType type;
            switch (hoursRemaining) {
                case 24:
                    type = NotificationType.ASSIGNMENT_DEADLINE_24H;
                    break;
                case 12:
                    type = NotificationType.ASSIGNMENT_DEADLINE_12H;
                    break;
                case 1:
                    type = NotificationType.ASSIGNMENT_DEADLINE_1H;
                    break;
                default:
                    log.warn("Unsupported hoursRemaining value: {}", hoursRemaining);
                    return;
            }
            
            String title = "Assignment Due in " + hoursRemaining + " Hours";
            String content = "Your assignment '" + assignment.getTitle() + "' is due soon.";
            
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("assignmentTitle", assignment.getTitle());
            data.put("hoursRemaining", hoursRemaining);
            data.put("dueDate", assignment.getDueDate().toString());
            
            createNotificationsForUsers(
                    type,
                    students,
                    title,
                    content,
                    assignmentId,
                    "assignment",
                    data
            );
            
            log.info("Sent assignment deadline notifications ({} hours) to {} students for assignment ID: {}", 
                    hoursRemaining, students.size(), assignmentId);
        } catch (Exception e) {
            log.error("Error sending assignment deadline notifications for assignment ID: " + assignmentId, e);
        }
    }

    @Override
    @Transactional
    @Async
    public void notifyQuizAvailable(Long quizId, List<Long> studentIds) {
        try {
            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
            
            Course course = courseRepository.findById(quiz.getCourse().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            
            List<User> students = new ArrayList<>();
            if (studentIds != null && !studentIds.isEmpty()) {
                students = userRepository.findAllById(studentIds);
            } else {
                // If no specific students provided, notify all enrolled students
                students = course.getStudents().stream().collect(Collectors.toList());
            }
            
            if (students.isEmpty()) {
                log.info("No students to notify for quiz availability in course ID: {}", course.getId());
                return;
            }
            
            String title = "New Quiz Available: " + quiz.getTitle();
            String content = "A new quiz is available in your course: " + course.getTitle();
            
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("quizTitle", quiz.getTitle());
            data.put("startDate", quiz.getStartDate() != null ? quiz.getStartDate().toString() : "Now");
            data.put("endDate", quiz.getEndDate() != null ? quiz.getEndDate().toString() : "No end date");
            data.put("timeLimit", quiz.getTimeLimit() != null ? quiz.getTimeLimit() + " minutes" : "No time limit");
            
            createNotificationsForUsers(
                    NotificationType.QUIZ_AVAILABLE,
                    students,
                    title,
                    content,
                    quizId,
                    "quiz",
                    data
            );
            
            log.info("Sent quiz availability notifications to {} students for quiz ID: {}", 
                    students.size(), quizId);
        } catch (Exception e) {
            log.error("Error sending quiz availability notifications for quiz ID: " + quizId, e);
        }
    }

    @Override
    @Transactional
    @Async
    public void notifyGradePosted(Long courseId, Long studentId, String assessmentType, String assessmentTitle) {
        try {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
            
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
            
            String title = "Grade Posted: " + assessmentTitle;
            String content = "Your grade for " + assessmentTitle + " in " + course.getTitle() + " has been posted.";
            
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("assessmentTitle", assessmentTitle);
            data.put("assessmentType", assessmentType);
            
            createNotification(
                    NotificationType.GRADE_POSTED,
                    student,
                    title,
                    content,
                    courseId,
                    "course",
                    data
            );
            
            log.info("Sent grade posted notification to student ID: {} for assessment: {} in course ID: {}", 
                    studentId, assessmentTitle, courseId);
        } catch (Exception e) {
            log.error("Error sending grade posted notification for student ID: " + studentId, e);
        }
    }

    @Override
    @Transactional
    @Async
    public void notifyCourseAnnouncement(Long courseId, String announcementTitle, String announcementContent) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
            
            List<User> students = course.getStudents().stream().collect(Collectors.toList());
            
            if (students.isEmpty()) {
                log.info("No students to notify for course announcement in course ID: {}", courseId);
                return;
            }
            
            String title = "Announcement: " + announcementTitle;
            String content = announcementContent;
            
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("announcementTitle", announcementTitle);
            data.put("announcementContent", announcementContent);
            data.put("instructorName", course.getInstructor().getFullName());
            
            createNotificationsForUsers(
                    NotificationType.COURSE_ANNOUNCEMENT,
                    students,
                    title,
                    content,
                    courseId,
                    "course",
                    data
            );
            
            log.info("Sent course announcement notifications to {} students for course ID: {}", 
                    students.size(), courseId);
        } catch (Exception e) {
            log.error("Error sending course announcement notifications for course ID: " + courseId, e);
        }
    }

    @Override
    @Transactional
    @Async
    public void notifyForumReply(Long forumPostId, Long replyId, Long authorId) {
        try {
            ForumPost originalPost = forumPostRepository.findById(forumPostId)
                    .orElseThrow(() -> new ResourceNotFoundException("Forum post not found with id: " + forumPostId));
            
            ForumPost reply = forumPostRepository.findById(replyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Reply post not found with id: " + replyId));
            
            User replyAuthor = userRepository.findById(authorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + authorId));
            
            User originalAuthor = userRepository.findById(originalPost.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Original post author not found"));
            
            // Don't notify if the reply author is the same as the original post author
            if (originalAuthor.getId().equals(authorId)) {
                return;
            }
            
            String title = "New Reply to Your Forum Post";
            String content = replyAuthor.getFullName() + " replied to your post: " + 
                    (originalPost.getContent().length() > 50 ? 
                    originalPost.getContent().substring(0, 50) + "..." : 
                    originalPost.getContent());
            
            Map<String, Object> data = new HashMap<>();
            data.put("replyAuthorName", replyAuthor.getFullName());
            data.put("replyContent", reply.getContent());
            data.put("originalPostContent", originalPost.getContent());
            data.put("threadTitle", "Forum Discussion"); // Add thread title if available
            
            createNotification(
                    NotificationType.FORUM_REPLY,
                    originalAuthor,
                    title,
                    content,
                    forumPostId,
                    "forumPost",
                    data
            );
            
            log.info("Sent forum reply notification to user ID: {} for post ID: {}", 
                    originalAuthor.getId(), forumPostId);
        } catch (Exception e) {
            log.error("Error sending forum reply notification for post ID: " + forumPostId, e);
        }
    }

    @Override
    @Transactional
    @Async
    public void notifyForumMention(Long forumPostId, Long mentionedUserId, Long authorId) {
        try {
            ForumPost post = forumPostRepository.findById(forumPostId)
                    .orElseThrow(() -> new ResourceNotFoundException("Forum post not found with id: " + forumPostId));
            
            User mentionedUser = userRepository.findById(mentionedUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mentioned user not found with id: " + mentionedUserId));
            
            User author = userRepository.findById(authorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + authorId));
            
            // Don't notify if the author mentioned themselves
            if (mentionedUserId.equals(authorId)) {
                return;
            }
            
            String title = "You Were Mentioned in a Forum Post";
            String content = author.getFullName() + " mentioned you in a forum post.";
            
            Map<String, Object> data = new HashMap<>();
            data.put("authorName", author.getFullName());
            data.put("postContent", post.getContent());
            data.put("threadTitle", "Forum Discussion"); // Add thread title if available
            
            createNotification(
                    NotificationType.FORUM_MENTION,
                    mentionedUser,
                    title,
                    content,
                    forumPostId,
                    "forumPost",
                    data
            );
            
            log.info("Sent forum mention notification to user ID: {} from author ID: {}", 
                    mentionedUserId, authorId);
        } catch (Exception e) {
            log.error("Error sending forum mention notification to user ID: " + mentionedUserId, e);
        }
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