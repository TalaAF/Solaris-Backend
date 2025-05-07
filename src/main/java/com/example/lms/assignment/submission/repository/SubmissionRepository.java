package com.example.lms.assignment.submission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lms.assignment.submission.model.Submission;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    // Find submissions for an assignment
    List<Submission> findByAssignmentId(Long assignmentId);
    
    // Count submissions for an assignment
    Long countByAssignmentId(Long assignmentId);
    
    // Find submissions by user ID
    List<Submission> findByUserId(Long userId);
    
    // Find submission for a specific assignment and user
    Submission findByAssignmentIdAndUserId(Long assignmentId, Long userId);
}