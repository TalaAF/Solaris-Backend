package com.example.lms.assessment.model;

public enum AttemptStatus {
    IN_PROGRESS,    // Quiz attempt started but not submitted
    COMPLETED,      // Quiz attempt completed and submitted
    TIMED_OUT,      // Quiz attempt timed out
    ABANDONED       // Quiz attempt was abandoned
}