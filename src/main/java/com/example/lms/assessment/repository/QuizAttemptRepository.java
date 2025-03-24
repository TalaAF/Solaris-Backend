package com.example.lms.assessment.repository;

import com.example.lms.assessment.model.AttemptStatus;
import com.example.lms.assessment.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    
    List<QuizAttempt> findByQuizId(Long quizId);
    
    List<QuizAttempt> findByStudentId(Long studentId);
    
    List<QuizAttempt> findByQuizIdAndStudentId(Long quizId, Long studentId);
    
    Optional<QuizAttempt> findByQuizIdAndStudentIdAndStatus(Long quizId, Long studentId, AttemptStatus status);
    
    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.status = 'COMPLETED'")
    Long countCompletedAttempts(@Param("quizId") Long quizId);
    
    @Query("SELECT AVG(qa.percentageScore) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.status = 'COMPLETED'")
    Double calculateAverageScore(@Param("quizId") Long quizId);
    
    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.passed = true AND qa.status = 'COMPLETED'")
    Long countPassedAttempts(@Param("quizId") Long quizId);
    
    @Query("SELECT MAX(qa.percentageScore) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.student.id = :studentId AND qa.status = 'COMPLETED'")
    Double findHighestScoreByQuizIdAndStudentId(@Param("quizId") Long quizId, @Param("studentId") Long studentId);

    Long countByQuizId(Long quizId);
}