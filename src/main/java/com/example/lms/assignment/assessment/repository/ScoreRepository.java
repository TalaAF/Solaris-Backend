package com.example.lms.assignment.assessment.repository;

import com.example.lms.assignment.assessment.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository extends JpaRepository<Score, Long> {
}