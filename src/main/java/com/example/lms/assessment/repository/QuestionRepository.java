package com.example.lms.assessment.repository;

import com.example.lms.assessment.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    List<Question> findByQuizId(Long quizId);
    
    List<Question> findByQuizIdOrderByOrderIndexAsc(Long quizId);
    
    @Query("SELECT MAX(q.orderIndex) FROM Question q WHERE q.quiz.id = :quizId")
    Integer findMaxOrderIndexByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT COUNT(sa) * 1.0 / (SELECT COUNT(s) FROM StudentAnswer s WHERE s.question.id = :questionId) FROM StudentAnswer sa WHERE sa.question.id = :questionId AND sa.isCorrect = true")
    Double calculateCorrectPercentage(@Param("questionId") Long questionId);
    
    void deleteByQuizId(Long quizId);
}