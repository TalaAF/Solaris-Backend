package com.example.lms.course.controller;

import com.example.lms.course.dto.StudentCourseDTO;
import com.example.lms.course.service.StudentDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student-dashboard")
@Tag(name = "Student Dashboard", description = "APIs for student dashboard views")
public class StudentDashboardController {

    @Autowired
    private StudentDashboardService studentDashboardService;

    /**
     * Get all courses for a student categorized by registration status
     */
    @GetMapping("/courses/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or #studentId == authentication.principal.id")
    @Operation(summary = "Get student courses", description = "Get all courses for a student categorized by registration status")
    public ResponseEntity<Map<String, List<StudentCourseDTO>>> getStudentCourses(
            @Parameter(description = "Student ID", required = true)
            @PathVariable Long studentId) {
        
        Map<String, List<StudentCourseDTO>> courses = studentDashboardService.getStudentCourses(studentId);
        return ResponseEntity.ok(courses);
    }

    /**
     * Get current term summary for a student
     */
    @GetMapping("/current-term/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or #studentId == authentication.principal.id")
    @Operation(summary = "Get current term summary", description = "Get student's current term courses and credit information")
    public ResponseEntity<Map<String, Object>> getCurrentTermSummary(
            @Parameter(description = "Student ID", required = true)
            @PathVariable Long studentId) {
        
        Map<String, Object> termSummary = studentDashboardService.getCurrentTermSummary(studentId);
        return ResponseEntity.ok(termSummary);
    }
}