package com.example.lms.content.repository;

import com.example.lms.content.model.Module;
import com.example.lms.course.model.Course;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    /**
     * Find modules by course, ordered by sequence
     * 
     * @param course The course
     * @return List of modules
     */
    List<Module> findByCourseOrderBySequenceAsc(Course course);
    
    /**
     * Find the maximum sequence number for a course
     * 
     * @param courseId The course ID
     * @return The maximum sequence number
     */
    @Query("SELECT MAX(m.sequence) FROM Module m WHERE m.course.id = :courseId")
    Integer findMaxSequenceByCourseId(@Param("courseId") Long courseId);
}