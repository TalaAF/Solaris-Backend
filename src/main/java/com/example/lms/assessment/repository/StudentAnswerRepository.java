package com.example.lms.assessment.repository;

import com.example.lms.assessment.model.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    
    List<StudentAnswer> findByAttemptId(Long attemptId);
    
    Optional<StudentAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);
    
    List<StudentAnswer> findByQuestionId(Long questionId);
    
    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.question.id = :questionId AND sa.isCorrect = true")
    Long countCorrectAnswers(@Param("questionId") Long questionId);
    
    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.question.id = :questionId")
    Long countTotalAnswers(@Param("questionId") Long questionId);
    
    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.attempt.id = :attemptId AND sa.manuallyGraded = true AND sa.score IS NOT NULL")
    Long countGradedAnswers(@Param("attemptId") Long attemptId);
    
    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.attempt.id = :attemptId AND sa.manuallyGraded = true")
    Long countManualGradingRequired(@Param("attemptId") Long attemptId);
}