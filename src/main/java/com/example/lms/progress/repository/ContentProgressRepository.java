package com.example.lms.progress.repository;

import com.example.lms.progress.model.ContentProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentProgressRepository extends JpaRepository<ContentProgress, Long> {

    Optional<ContentProgress> findByStudentIdAndContentId(Long studentId, Long contentId);

    List<ContentProgress> findByStudentId(Long studentId);
}
