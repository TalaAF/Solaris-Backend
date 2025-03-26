package com.example.lms.assignment.assignments.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lms.assignment.assignments.model.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}