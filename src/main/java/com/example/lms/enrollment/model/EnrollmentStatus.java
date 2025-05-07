package com.example.lms.enrollment.model;

public enum EnrollmentStatus {
    PENDING,     // Waiting for approval
    APPROVED,    // Enrollment approved and active (this will be "Registered")
    REJECTED,    // Enrollment was rejected
    IN_PROGRESS, // Student is actively taking the course (also "Registered")
    COMPLETED,   // Course requirements are finished ("Completed")
    CANCELLED,   // Enrollment was cancelled
    EXPIRED      // Enrollment period has ended
}