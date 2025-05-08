package com.example.lms.assignment.assignments.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.lms.assignment.assignments.model.Assignment;

import java.time.LocalDateTime;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    // Find assignments by course ID
    List<Assignment> findByCourseId(Long courseId);
    
    // Find assignments by course ID with pagination
    Page<Assignment> findByCourseId(Long courseId, Pageable pageable);
    
    // Find assignments by published status with pagination
    Page<Assignment> findByPublished(boolean published, Pageable pageable);
    
    // Find assignments by course ID and published status with pagination
    Page<Assignment> findByCourseIdAndPublished(Long courseId, boolean published, Pageable pageable);
    
    // Find assignments with due dates between the specified start and end times
    List<Assignment> findByDueDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Search assignments by title or description
    @Query("SELECT a FROM Assignment a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Assignment> searchAssignments(@Param("search") String search, Pageable pageable);
    
    // Search assignments by title or description and filter by course ID
    @Query("SELECT a FROM Assignment a WHERE (LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND a.courseId = :courseId")
    Page<Assignment> searchAssignmentsByCourse(@Param("search") String search, @Param("courseId") Long courseId, Pageable pageable);
}