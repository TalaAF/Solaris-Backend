package com.example.lms.enrollment.service;

import org.springframework.stereotype.Service;

@Service
public class EnrollmentNotificationService {

    public void notifyEnrollment(Long studentId, Long courseId) {
        // For simplicity, we'll log the notification here.
        // You can extend this to send an email or system message.
        System.out.println("Notification: Student " + studentId + " has successfully enrolled in course " + courseId);
    }
}
