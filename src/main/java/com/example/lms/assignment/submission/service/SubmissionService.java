package com.example.lms.assignment.submission.service;

import com.example.lms.assignment.assignments.model.Assignment;
import com.example.lms.assignment.assignments.model.Score;
import com.example.lms.assignment.assignments.repository.AssignmentRepository;
import com.example.lms.assignment.assignments.repository.ScoreRepository;
import com.example.lms.assignment.submission.model.Submission;
import com.example.lms.assignment.submission.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    private final String uploadDir = "uploads/";
    private final String[] allowedFileTypes = {".pdf"};

    public Submission submitAssignment(Long assignmentId, Long studentId, MultipartFile file) throws IOException {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + assignmentId));

        if (LocalDateTime.now().isAfter(assignment.getDueDate())) {
            throw new IllegalStateException("Submission is past due date");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !isAllowedFileType(fileName)) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        String newFileName = studentId + "_" + System.currentTimeMillis() + "_" + fileName;
        String filePath = uploadDir + newFileName;
        File dest = new File(filePath);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        file.transferTo(dest);

        Submission submission = new Submission();
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setFilePath(filePath);
        submission.setSubmissionDate(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    public Submission reviewSubmission(Long submissionId, String feedback, Integer grade) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found with ID: " + submissionId));

        if (grade != null && (grade < 0 || grade > 100)) {
            throw new IllegalArgumentException("Grade must be between 0 and 100");
        }

        submission.setFeedback(feedback);
        submission.setGrade(grade);
        submission = submissionRepository.save(submission);

        if (grade != null) {
            Score score = new Score();
            score.setStudentId(submission.getStudentId());
            score.setAssignmentId(submission.getAssignmentId());
            score.setScore(grade);
            score.setGradedDate(LocalDateTime.now());
            scoreRepository.save(score);
        }

        return submission;
    }

    private boolean isAllowedFileType(String fileName) {
        for (String type : allowedFileTypes) {
            if (fileName.toLowerCase().endsWith(type)) {
                return true;
            }
        }
        return false;
    }
}