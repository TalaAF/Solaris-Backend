package com.example.lms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private Map<String, Long> usersByRole;
    private Map<String, Long> usersByDepartment;
    private long totalCourses;
    private long totalDepartments;
    private long newUsersThisMonth;
}