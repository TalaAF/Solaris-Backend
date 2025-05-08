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
    List<Submission> findByStudentId(Long studentId);
    
    // Find submission for a specific assignment and user
    Submission findByAssignmentIdAndStudentId(Long assignmentId, Long studentId); // FIXED: Using studentId
}