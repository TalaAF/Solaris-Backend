package com.example.lms.notification.service;

import com.example.lms.notification.model.CompletionNotification;
import com.example.lms.notification.repository.CompletionNotificationRepository;
import com.example.lms.user.model.User;
import com.example.lms.course.model.Course;
import com.example.lms.logging.service.UserActivityLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CompletionNotificationService {
    @Autowired
    private CompletionNotificationRepository notificationRepository; // Injected repository
    
    @Autowired
    private EmailService emailService;
    @Autowired
private UserActivityLogService logService;
    
    public void sendCompletionNotification(User student, Course course, String message) {
        CompletionNotification notification = new CompletionNotification();
        notification.setStudent(student);
        notification.setCourse(course);
        notification.setMessage(message);
        
        // Save notification using repository instance
        notificationRepository.save(notification); 
        logService.logActivity(student, "COURSE_COMPLETION", "Completed course: " + course.getName());
        // Send email notification
        String subject = "Course Completion Notification";
        String emailMessage = "Dear " + student.getFullName() + ",\n\n" + message;
        emailService.sendEmail(student.getEmail(), subject, emailMessage);
    }

    public List<CompletionNotification> getNotificationsForStudent(Long studentId) {
        return notificationRepository.findByStudentId(studentId);
    }
}
