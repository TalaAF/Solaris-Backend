package com.example.lms.user.service;

import com.example.lms.Department.repository.DepartmentRepository;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.security.repository.RoleRepository;
import com.example.lms.user.dto.AdminDashboardDTO;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardData() {
        // Get user counts
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long inactiveUsers = totalUsers - activeUsers;
        
        // Get users by role
        Map<String, Long> usersByRole = new HashMap<>();
        roleRepository.findAll().forEach(role -> {
            long count = userRepository.countByRolesContaining(role);
            usersByRole.put(role.getName(), count);
        });
        
        // Get users by department
        Map<String, Long> usersByDepartment = new HashMap<>();
        departmentRepository.findAll().forEach(department -> {
            long count = userRepository.countByDepartment(department);
            usersByDepartment.put(department.getName(), count);
        });
        
        // Get other counts
        long totalCourses = courseRepository.count();
        long totalDepartments = departmentRepository.count();
        
        // Get new users this month
        LocalDateTime firstDayOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(firstDayOfMonth);
        
        // Build dashboard DTO
        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .usersByRole(usersByRole)
                .usersByDepartment(usersByDepartment)
                .totalCourses(totalCourses)
                .totalDepartments(totalDepartments)
                .newUsersThisMonth(newUsersThisMonth)
                .build();
    }
}