package com.example.lms.assignment.assessment.repository;

import com.example.lms.assignment.assessment.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}