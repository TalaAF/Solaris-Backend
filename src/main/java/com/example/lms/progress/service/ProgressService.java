package com.example.lms.progress.service;

import com.example.lms.progress.dto.ProgressDTO;
import com.example.lms.progress.model.Progress;
import com.example.lms.progress.repository.ProgressRepository;
import com.example.lms.progress.assembler.ProgressAssembler;
import com.example.lms.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private ProgressAssembler progressAssembler;

    public ProgressDTO updateProgress(Long studentId, Long courseId, Double progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100.");
        }

        Progress existingProgress = progressRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found"));

        existingProgress.setProgress(progress);
        existingProgress.setLastUpdated(LocalDateTime.now());

        progressRepository.save(existingProgress);

        return progressAssembler.toDTO(existingProgress);
    }

    public ProgressDTO getProgress(Long studentId, Long courseId) {
        Progress existingProgress = progressRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found"));

        return progressAssembler.toDTO(existingProgress);
    }
}
