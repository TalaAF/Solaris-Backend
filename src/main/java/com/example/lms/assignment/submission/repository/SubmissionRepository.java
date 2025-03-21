package com.example.lms.assignment.submission.repository;

import com.example.lms.assignment.submission.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}