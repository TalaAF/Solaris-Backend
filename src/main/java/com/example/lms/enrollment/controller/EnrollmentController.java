package com.example.lms.enrollment.controller;

import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @PostMapping("/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentDTO enrollStudent(@RequestParam Long studentId, @RequestParam Long courseId) {
        return enrollmentService.enrollStudent(studentId, courseId);
    }

    @GetMapping("/student/{studentId}")
    public List<EnrollmentDTO> getEnrollmentsForStudent(@PathVariable Long studentId) {
        return enrollmentService.getEnrollmentsForStudent(studentId);
    }

    @GetMapping("/course/{courseId}")
    public List<EnrollmentDTO> getEnrollmentsForCourse(@PathVariable Long courseId) {
        return enrollmentService.getEnrollmentsForCourse(courseId);
    }
    
    @PatchMapping("/{studentId}/{courseId}/progress")
public ResponseEntity<EnrollmentDTO> updateProgress(
        @PathVariable Long studentId,
        @PathVariable Long courseId,
        @RequestParam Double progress) {
    
    EnrollmentDTO updatedEnrollment = enrollmentService.updateProgress(studentId, courseId, progress);
    return ResponseEntity.ok(updatedEnrollment);
}

}
