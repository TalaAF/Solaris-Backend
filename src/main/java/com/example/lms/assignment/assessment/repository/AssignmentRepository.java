package com.example.lms.assignment.assessment.repository;

import com.example.lms.assignment.assessment.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}