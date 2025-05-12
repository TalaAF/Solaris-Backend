package com.example.lms.progress.service;

import com.example.lms.content.model.Content;
import com.example.lms.content.repository.ContentRepository;
import com.example.lms.progress.model.ContentProgress;
import com.example.lms.progress.repository.ContentProgressRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;

import jakarta.transaction.Transactional;

import com.example.lms.common.Exception.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Service
public class ContentProgressService {

    private static final Logger log = LoggerFactory.getLogger(ContentProgressService.class);

    @Autowired
    private ContentProgressRepository contentProgressRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ContentRepository contentRepository;

    // Update progress for a specific student and content
    @Transactional
    public ContentProgress updateProgress(Long studentId, Long contentId, Double progress) {
        // Validate progress range
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100.");
        }

        // Find student and content
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
            
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        // Find existing content progress by studentId and contentId
        ContentProgress contentProgress = contentProgressRepository.findByStudent_IdAndContent_Id(studentId, contentId)
                .orElse(new ContentProgress()); // Create new if not found

        // Set or update relationships
        contentProgress.setStudent(student);
        contentProgress.setContent(content);
        
        // Update progress and timestamp
        contentProgress.setProgress(progress);
        contentProgress.setLastUpdated(LocalDateTime.now());
        
        // Mark as completed if progress is 100%
        if (progress >= 100.0) {
            contentProgress.setCompleted(true);
        }

        return contentProgressRepository.save(contentProgress); // Save updated progress
    }

    // Get content progress for a specific student
    public List<ContentProgress> getStudentProgress(Long studentId) {
        return contentProgressRepository.findByStudent_Id(studentId);
    }

    // Calculate overall progress for a student
    public Double calculateOverallProgress(Long studentId) {
        List<ContentProgress> progressList = contentProgressRepository.findByStudent_Id(studentId);

        if (progressList.isEmpty()) {
            return 0.0;
        }

        double totalProgress = progressList.stream()
                                          .mapToDouble(ContentProgress::getProgress) // Sum up the progress values
                                          .sum();

        return totalProgress / progressList.size(); // Calculate the average progress
    }
    
    // Record that a student has viewed a content
    @Transactional
    public void recordContentView(Long studentId, Long contentId) {
        log.info("Recording view for content {} by student {}", contentId, studentId);
        
        try {
            // Find student and content
            User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
                
            Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));
        
            // Find or create progress record
            ContentProgress progress = contentProgressRepository.findByStudent_IdAndContent_Id(studentId, contentId)
                .orElse(new ContentProgress());
        
            // Set key relationships
            progress.setStudent(student);
            progress.setContent(content);
        
            // IMPORTANT: If using a legacy database schema that requires enrollment_id
            // Add a dummy value or find a real enrollment - this is a workaround
            if (progress.getId() == null) {
                progress.setEnrollmentId(1L); // Use a default value to satisfy NOT NULL constraint
            }
        
            // Set progress tracking fields
            if (progress.getFirstViewed() == null) {
                progress.setFirstViewed(LocalDateTime.now());
            }
        
            progress.setLastViewed(LocalDateTime.now());
            progress.setViewCount(progress.getViewCount() != null ? progress.getViewCount() + 1 : 1);
            progress.setLastUpdated(LocalDateTime.now());
        
            // Update progress percentage based on views
            if (progress.getProgress() == null) {
                progress.setProgress(25.0); // First view
            } else if (progress.getProgress() < 100.0) {
                // Increment by 25% per view
                progress.setProgress(Math.min(progress.getProgress() + 25.0, 100.0));
            }
        
            // Mark as completed if 100%
            if (progress.getProgress() >= 100.0) {
                progress.setCompleted(true);
            }
        
            // Save changes
            contentProgressRepository.save(progress);
            log.info("Successfully updated progress for content {} to {}%", contentId, progress.getProgress());
        } catch (Exception e) {
            log.error("Error recording content view: {}", e.getMessage(), e);
            throw e; // Rethrow to be caught by controller
        }
    }

    /**
     * Get progress for a student in a course
     */
    public Map<String, Object> getCourseProgress(Long studentId, Long courseId) {
        log.info("Getting course progress for student {} in course {}", studentId, courseId);
        
        try {
            // Verify student exists
            if (!userRepository.existsById(studentId)) {
                throw new ResourceNotFoundException("Student not found with id: " + studentId);
            }
            
            // For now create a basic response since we don't have all repository methods yet
            Map<String, Object> result = new HashMap<>();
            result.put("courseId", courseId);
            result.put("progress", 0.0);  // Will be calculated based on content progress
            result.put("lastUpdated", LocalDateTime.now());
            result.put("completedItems", new ArrayList<>()); // Will contain completed content items
            
            // This is a simplified implementation - you should enhance with actual data
            // from the DB once you have the repository methods working
            
            log.info("Course progress data fetched successfully");
            return result;
        } catch (Exception e) {
            log.error("Error getting course progress: {}", e.getMessage(), e);
            throw e; // Rethrow to be caught by controller
        }
    }
}
