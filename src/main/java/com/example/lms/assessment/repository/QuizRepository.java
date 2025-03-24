package com.example.lms.assessment.repository;

import com.example.lms.assessment.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
    List<Quiz> findByCourseId(Long courseId);
    
    List<Quiz> findByCourseIdAndPublishedTrue(Long courseId);
    
    @Query("SELECT q FROM Quiz q WHERE q.course.id = :courseId AND q.published = true " +
           "AND (:now BETWEEN q.startDate AND q.endDate OR (q.startDate <= :now AND q.endDate IS NULL) " +
           "OR (q.startDate IS NULL AND q.endDate >= :now) OR (q.startDate IS NULL AND q.endDate IS NULL))")
    List<Quiz> findAvailableQuizzesByCourseId(@Param("courseId") Long courseId, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(q) > 0 FROM Quiz q WHERE q.title = :title AND q.course.id = :courseId AND (:quizId IS NULL OR q.id != :quizId)")
    boolean existsByTitleAndCourseIdAndIdNot(@Param("title") String title, @Param("courseId") Long courseId, @Param("quizId") Long quizId);
}