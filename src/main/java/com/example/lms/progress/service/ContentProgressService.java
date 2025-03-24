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

    // Update progress for a specific student and content
    public ContentProgress updateProgress(Long studentId, Long contentId, Double progress) {
        // Validate progress range
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100.");
        }

        // Find existing content progress by studentId and contentId
        ContentProgress contentProgress = contentProgressRepository.findByEnrollmentStudentIdAndContentId(studentId, contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content progress not found for studentId " + studentId + " and contentId " + contentId));

        // Update progress and timestamp
        contentProgress.setProgress(progress);
        contentProgress.setLastUpdated(LocalDateTime.now());

        return contentProgressRepository.save(contentProgress); // Save updated progress
    }

    // Get content progress for a specific student
    public List<ContentProgress> getStudentProgress(Long studentId) {
        return contentProgressRepository.findByEnrollmentStudentId(studentId);
    }

    // Calculate overall progress for a student
    public Double calculateOverallProgress(Long studentId) {
        List<ContentProgress> progressList = contentProgressRepository.findByEnrollmentStudentId(studentId);

        if (progressList.isEmpty()) {
            return 0.0;
        }

        double totalProgress = progressList.stream()
                                           .mapToDouble(ContentProgress::getProgress) // Sum up the progress values
                                           .sum();

        return totalProgress / progressList.size(); // Calculate the average progress
    }
}
