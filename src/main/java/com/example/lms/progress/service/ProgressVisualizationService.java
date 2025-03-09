package com.example.lms.progress.service;

import com.example.lms.progress.model.ContentProgress;
import com.example.lms.progress.repository.ContentProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgressVisualizationService {

    @Autowired
    private ContentProgressRepository contentProgressRepository;

    public Double calculateOverallProgress(Long studentId) {
        // Use the correct repository method: findByEnrollmentStudentId
        List<ContentProgress> progressList = contentProgressRepository.findByEnrollmentStudentId(studentId);

        if (progressList.isEmpty()) {
            return 0.0;
        }

        double totalProgress = progressList.stream().mapToDouble(ContentProgress::getProgress).sum();
        return totalProgress / progressList.size(); // Calculate average progress
    }
}
