package com.example.lms.analytics.dto;

import java.time.LocalDateTime;

public record SystemAnalyticsDTO(
    Long totalUsers,
    Long totalCourses,
    Long totalLogins,
    Long activeUsers,
    LocalDateTime lastUpdated
) {}
