package com.example.lms.assignment.dashboard.controller;

import com.example.lms.assignment.dashboard.dto.StudentDashboardDTO;
import com.example.lms.assignment.dashboard.mapper.DashboardMapper;
import com.example.lms.assignment.dashboard.model.StudentDashboard;
import com.example.lms.assignment.dashboard.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "APIs for student dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DashboardMapper dashboardMapper;

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get student dashboard", description = "Retrieves the dashboard for a specific student, including upcoming assignment deadlines and recent forum activity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Invalid student ID")
    })
    public ResponseEntity<StudentDashboardDTO> getStudentDashboard(@PathVariable Long studentId) {
        StudentDashboard dashboard = dashboardService.getStudentDashboard(studentId);
        return ResponseEntity.ok(dashboardMapper.toStudentDashboardDTO(dashboard));
    }
}