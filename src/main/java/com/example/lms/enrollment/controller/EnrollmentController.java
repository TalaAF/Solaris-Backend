package com.example.lms.enrollment.controller;

import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Enroll a student in a course
     */
    @PostMapping("/enroll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or @userRepository.findById(#studentId).orElse(new com.example.lms.user.model.User()).getEmail() == authentication.name")
    public ResponseEntity<EnrollmentDTO> enrollStudent(
            @RequestParam Long studentId, 
            @RequestParam Long courseId) {
        EnrollmentDTO enrollment = enrollmentService.enrollStudent(studentId, courseId);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
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
    @PatchMapping("/{studentId}/{courseId}/progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or @userRepository.findById(#studentId).orElse(new com.example.lms.user.model.User()).getEmail() == authentication.name")
    public ResponseEntity<EnrollmentDTO> updateProgress(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestParam Double progress) {
        
        EnrollmentDTO updatedEnrollment = enrollmentService.updateProgress(studentId, courseId, progress);
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
     * Unenroll a student from a course
     */
    @DeleteMapping("/{studentId}/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or @userRepository.findById(#studentId).orElse(new com.example.lms.user.model.User()).getEmail() == authentication.name")
    public ResponseEntity<Void> unenrollStudent(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        
        enrollmentService.unenrollStudent(studentId, courseId);
        return ResponseEntity.noContent().build();
    }
}