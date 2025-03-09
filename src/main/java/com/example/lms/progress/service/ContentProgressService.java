package com.example.lms.progress.service;

import com.example.lms.progress.model.ContentProgress;
import com.example.lms.progress.repository.ContentProgressRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentProgressService {

    @Autowired
    private ContentProgressRepository contentProgressRepository;

    public ContentProgress updateProgress(Long studentId, Long contentId, Double progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100.");
        }

        ContentProgress contentProgress = contentProgressRepository.findByStudentIdAndContentId(studentId, contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content progress not found"));

        contentProgress.setProgress(progress);
        contentProgress.setLastUpdated(LocalDateTime.now());

        return contentProgressRepository.save(contentProgress);
    }

    public List<ContentProgress> getStudentProgress(Long studentId) {
        return contentProgressRepository.findByStudentId(studentId);
    }
}
