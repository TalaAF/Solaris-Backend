package com.example.lms.assignment.assignments.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lms.assignment.assignments.model.Score;

public interface ScoreRepository extends JpaRepository<Score, Long> {
}