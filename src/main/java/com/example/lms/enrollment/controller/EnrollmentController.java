package com.example.lms.enrollment.controller;

import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.dto.EnrollmentRequest;        // Add this
import com.example.lms.enrollment.dto.BatchEnrollmentRequest;   // Add this
import com.example.lms.enrollment.dto.StatusUpdateRequest;      // Add this
import com.example.lms.enrollment.dto.ProgressUpdateRequest;    // Add this
import com.example.lms.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Enroll a student in a course
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or @userRepository.findById(#enrollmentRequest.userId).orElse(new com.example.lms.user.model.User()).getEmail() == authentication.name")
    public ResponseEntity<EnrollmentDTO> enrollStudent(@RequestBody EnrollmentRequest enrollmentRequest) {
        EnrollmentDTO enrollment = enrollmentService.enrollStudent(
            enrollmentRequest.getUserId(), 
            enrollmentRequest.getCourseId()
        );
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }

    /**
     * Enroll multiple students in a course
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<List<EnrollmentDTO>> enrollMultipleStudents(@RequestBody BatchEnrollmentRequest request) {
        List<EnrollmentDTO> enrollments = enrollmentService.enrollMultipleStudents(
            request.getCourseId(), 
            request.getUserIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollments);
    }

    /**
     * Update enrollment status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<EnrollmentDTO> updateEnrollmentStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {
        EnrollmentDTO updatedEnrollment = enrollmentService.updateEnrollmentStatus(id, request.getStatus());
        return ResponseEntity.ok(updatedEnrollment);
    }

    /**
     * Get all enrollments for a student
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or @userRepository.findById(#studentId).orElse(new com.example.lms.user.model.User()).getEmail() == authentication.name")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsForStudent(@PathVariable Long studentId) {
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsForStudent(studentId);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Get all enrollments for a course
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsForCourse(@PathVariable Long courseId) {
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsForCourse(courseId);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Update enrollment progress
     */
    @PatchMapping("/{id}/progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<EnrollmentDTO> updateProgress(
            @PathVariable Long id,
            @RequestBody ProgressUpdateRequest request) {
        
        EnrollmentDTO updatedEnrollment = enrollmentService.updateProgressById(
            id, 
            request.getProgress(), 
            request.getGrade()
        );
        return ResponseEntity.ok(updatedEnrollment);
    }
    
    /**
     * Get a specific enrollment
     */
    @GetMapping("/{studentId}/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or @userRepository.findById(#studentId).orElse(new com.example.lms.user.model.User()).getEmail() == authentication.name")
    public ResponseEntity<EnrollmentDTO> getEnrollment(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        
        EnrollmentDTO enrollment = enrollmentService.getEnrollment(studentId, courseId);
        return ResponseEntity.ok(enrollment);
    }
    
    /**
     * Complete a course enrollment
     */
    @PostMapping("/{studentId}/{courseId}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<EnrollmentDTO> completeCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        
        EnrollmentDTO completedEnrollment = enrollmentService.completeCourse(studentId, courseId);
        return ResponseEntity.ok(completedEnrollment);
    }
    
    /**
     * Delete an enrollment by ID (permanent deletion)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<Map<String, Object>> deleteEnrollment(@PathVariable Long id) {
        boolean deleted = enrollmentService.deleteEnrollment(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", deleted);
        response.put("message", "Enrollment successfully deleted");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Unenroll a student (sets status to CANCELLED but doesn't delete record)
     */
    @DeleteMapping("/{id}/unenroll")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> unenrollStudent(@PathVariable Long id) {
        enrollmentService.unenrollStudentById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Student successfully unenrolled");
        
        return ResponseEntity.ok(response);
    }
}