package com.example.lms.assignment.submission.controller;

import com.example.lms.assignment.submission.model.Submission;
import com.example.lms.assignment.submission.service.SubmissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/submissions")
@Tag(name = "Submission", description = "APIs for managing assignment submissions")
@SecurityRequirement(name = "bearerAuth")
public class SubmissionController {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);

    @Autowired
    private SubmissionService submissionService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Upload an assignment submission", description = "Allows a student to upload a file for an assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Submission uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or file type"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Submission> submitAssignment(
            @RequestParam("assignmentId") Long assignmentId,
            @RequestParam("studentId") Long studentId,
            @RequestParam("file") MultipartFile file) throws IOException {
        logger.info("Received submission request: assignmentId={}, studentId={}", assignmentId, studentId);
        Submission submission = submissionService.submitAssignment(assignmentId, studentId, file);
        logger.info("Submission created successfully: id={}", submission.getId());
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/{submissionId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    @Operation(
    summary = "Get submission by ID",
    description = "Retrieves a specific submission by its ID. Accessible to STUDENT (own submissions only), INSTRUCTOR and ADMIN."
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Submission retrieved successfully"),
    @ApiResponse(responseCode = "403", description = "Unauthorized access"),
    @ApiResponse(responseCode = "404", description = "Submission not found")
})
    public ResponseEntity<Submission> getSubmission(@PathVariable Long submissionId) {
        logger.info("Fetching submission with id={}", submissionId);
        Submission submission = submissionService.getSubmission(submissionId);
        logger.info("Submission retrieved: id={}", submission.getId());
        return ResponseEntity.ok(submission);
    }

    @PatchMapping("/{submissionId}/review")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Review an assignment submission", description = "Allows an instructor to provide feedback and grade a submission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Submission reviewed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Submission not found")
    })
    public ResponseEntity<Submission> reviewSubmission(
            @PathVariable Long submissionId,
            @RequestBody ReviewRequest reviewRequest) {
        logger.info("Reviewing submission: submissionId={}, grade={}, feedback={}",
                submissionId, reviewRequest.getGrade(), reviewRequest.getFeedback());
        Submission submission = submissionService.reviewSubmission(
                submissionId, reviewRequest.getFeedback(), reviewRequest.getGrade());
        logger.info("Submission reviewed successfully: id={}", submission.getId());
        return ResponseEntity.ok(submission);
    }
}

// DTO for the review request
class ReviewRequest {
    private String feedback;
    private Integer grade;

    // Getters and setters
    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }
}