package com.example.lms.analytics.mapper;

import com.example.lms.analytics.dto.SystemAnalyticsDTO;
import com.example.lms.analytics.model.SystemAnalytics;

public class SystemAnalyticsMapper {

    public static SystemAnalyticsDTO toDTO(SystemAnalytics analytics) {
        return new SystemAnalyticsDTO(
            analytics.getTotalUsers(),
            analytics.getTotalCourses(),
            analytics.getTotalLogins(),
            analytics.getActiveUsers(),
            analytics.getLastUpdated()
        );
    }

    public static SystemAnalytics toEntity(SystemAnalyticsDTO dto) {
        SystemAnalytics analytics = new SystemAnalytics();
        analytics.setTotalUsers(dto.totalUsers());
        analytics.setTotalCourses(dto.totalCourses());
        analytics.setTotalLogins(dto.totalLogins());
        analytics.setActiveUsers(dto.activeUsers());
        analytics.setLastUpdated(dto.lastUpdated());
        return analytics;
    }
}
