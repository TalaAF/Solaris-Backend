package com.example.lms.enrollment.model;

public enum EnrollmentStatus {
    PENDING,     // Waiting for approval (if enrollment requires it)
    APPROVED,    // Enrollment approved and active
    REJECTED,    // Enrollment was rejected
    IN_PROGRESS, // Student is actively taking the course
    COMPLETED,   // Course requirements are finished
    CANCELLED,   // Enrollment was cancelled
    EXPIRED      // Enrollment period has ended
}