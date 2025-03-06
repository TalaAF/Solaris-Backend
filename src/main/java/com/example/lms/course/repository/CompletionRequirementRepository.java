package com.example.lms.course.repository;

import com.example.lms.course.model.CompletionRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompletionRequirementRepository extends JpaRepository<CompletionRequirement, Long> {
    List<CompletionRequirement> findByCourseId(Long courseId);  // To find requirements for a specific course
}
