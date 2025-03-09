package com.example.lms.notification.service;

import com.example.lms.notification.model.CompletionNotification;
import com.example.lms.notification.repository.CompletionNotificationRepository;
import com.example.lms.user.model.User;
import com.example.lms.course.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CompletionNotificationService {

    @Autowired
    private CompletionNotificationRepository notificationRepository;

    public void sendCompletionNotification(User student, Course course, String message) {
        CompletionNotification notification = new CompletionNotification();
        notification.setStudent(student);
        notification.setCourse(course);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    public List<CompletionNotification> getNotificationsForStudent(Long studentId) {
        return notificationRepository.findByStudentId(studentId);
    }
}
