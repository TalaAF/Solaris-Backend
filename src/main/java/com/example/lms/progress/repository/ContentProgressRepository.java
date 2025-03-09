package com.example.lms.progress.repository;

import com.example.lms.progress.model.ContentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ContentProgressRepository extends JpaRepository<ContentProgress, Long> {

    // Find by studentId and contentId
    Optional<ContentProgress> findByEnrollmentStudentIdAndContentId(Long studentId, Long contentId);

    // Find all content progress for a specific student
    List<ContentProgress> findByEnrollmentStudentId(Long studentId);
}
