package com.example.lms.assessment.repository;

import com.example.lms.assessment.model.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
    
    List<AnswerOption> findByQuestionId(Long questionId);
    
    List<AnswerOption> findByQuestionIdOrderByOrderIndexAsc(Long questionId);
    
    @Query("SELECT MAX(ao.orderIndex) FROM AnswerOption ao WHERE ao.question.id = :questionId")
    Integer findMaxOrderIndexByQuestionId(@Param("questionId") Long questionId);
    
    @Query("SELECT COUNT(sa) FROM StudentAnswer sa JOIN sa.selectedOptions ao WHERE ao.id = :optionId")
    Long countTimesSelected(@Param("optionId") Long optionId);
    
    void deleteByQuestionId(Long questionId);
}