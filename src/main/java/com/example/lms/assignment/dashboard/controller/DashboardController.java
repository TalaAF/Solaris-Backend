package com.example.lms.assignment.dashboard.controller;

import com.example.lms.assignment.dashboard.dto.StudentDashboardDTO;
import com.example.lms.assignment.dashboard.mapper.DashboardMapper;
import com.example.lms.assignment.dashboard.model.StudentDashboard;
import com.example.lms.assignment.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DashboardMapper dashboardMapper;

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentDashboardDTO> getStudentDashboard(@PathVariable Long studentId) {
        StudentDashboard dashboard = dashboardService.getStudentDashboard(studentId);
        return ResponseEntity.ok(dashboardMapper.toStudentDashboardDTO(dashboard));
    }
}