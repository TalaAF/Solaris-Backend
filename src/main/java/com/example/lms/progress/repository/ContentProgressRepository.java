package com.example.lms.progress.repository;

import com.example.lms.progress.model.ContentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentProgressRepository extends JpaRepository<ContentProgress, Long> {

    // Changed to use underscore notation for nested properties
    Optional<ContentProgress> findByStudent_IdAndContent_Id(Long studentId, Long contentId);
    
    // Changed to use underscore notation for nested properties
    List<ContentProgress> findByStudent_Id(Long studentId);
    
    // JPQL queries remain the same
    @Query("SELECT cp FROM ContentProgress cp WHERE cp.student.id = :studentId AND cp.content.course.id = :courseId")
    List<ContentProgress> findByStudentIdAndCourseId(
        @Param("studentId") Long studentId,
        @Param("courseId") Long courseId
    );
    
    @Query("SELECT COUNT(cp) FROM ContentProgress cp WHERE cp.student.id = :studentId AND cp.content.course.id = :courseId AND cp.completed = true")
    Integer countCompletedContentByCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
