package com.example.lms.progress.repository;

import com.example.lms.progress.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    // Custom query to find progress by student and course
    Optional<Progress> findByStudentIdAndCourseId(Long studentId, Long courseId);
}
