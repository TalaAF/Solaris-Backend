package com.example.lms.assignment.submission.controller;

import com.example.lms.assignment.submission.dto.SubmissionDTO;
import com.example.lms.assignment.submission.mapper.SubmissionMapper;
import com.example.lms.assignment.submission.model.Submission;
import com.example.lms.assignment.submission.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private SubmissionMapper submissionMapper;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionDTO> uploadSubmission(
            @RequestParam("assignmentId") Long assignmentId,
            @RequestParam("studentId") Long studentId,
            @RequestParam("file") MultipartFile file) throws Exception {
        Submission submission = submissionService.submitAssignment(assignmentId, studentId, file);
        return ResponseEntity.ok(submissionMapper.toSubmissionDTO(submission));
    }

    @PutMapping("/{submissionId}/review")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<SubmissionDTO> reviewSubmission(
            @PathVariable Long submissionId,
            @RequestParam String feedback,
            @RequestParam Integer grade) {
        Submission submission = submissionService.reviewSubmission(submissionId, feedback, grade);
        return ResponseEntity.ok(submissionMapper.toSubmissionDTO(submission));
    }
}