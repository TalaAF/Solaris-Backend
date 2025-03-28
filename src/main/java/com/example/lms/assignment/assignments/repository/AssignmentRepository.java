package com.example.lms.assignment.assignments.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lms.assignment.assignments.model.Assignment;

import java.time.LocalDateTime;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    // Add this method to your existing AssignmentRepository.java

/**
 * Find assignments with due dates between the specified start and end times
 * Used for deadline notifications
 */
List<Assignment> findByDueDateBetween(LocalDateTime start, LocalDateTime end);
}