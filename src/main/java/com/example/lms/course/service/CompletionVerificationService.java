package com.example.lms.course.service;

import com.example.lms.course.model.CompletionRequirement;
import com.example.lms.course.model.Course;
import com.example.lms.progress.service.ContentProgressService;
import com.example.lms.course.repository.CompletionRequirementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompletionVerificationService {

    @Autowired
    private CompletionRequirementRepository completionRequirementRepository;

    @Autowired
    private ContentProgressService contentProgressService;

    public boolean verifyCompletion(Long studentId, Long courseId) {
        // Step 1: Retrieve completion requirements for the course
        List<CompletionRequirement> requirements = completionRequirementRepository.findByCourseId(courseId);
        if (requirements.isEmpty()) {
            return false; // No completion rules exist for this course
        }

        // Step 2: Check if the student meets the progress requirement
        double totalProgress = contentProgressService.calculateOverallProgress(studentId);
        for (CompletionRequirement requirement : requirements) {
            if (totalProgress < requirement.getRequiredProgress()) {
                return false; // Student hasn't met the progress requirement
            }

            // Step 3: Optionally check if quiz passing is required (if you have a quiz service, you can implement this check)
            // For now, we skip the quiz check
        }

        return true; // Student meets all the completion requirements
    }

    // Method to get all completion requirements for a course
    public List<CompletionRequirement> getCompletionRequirementsForCourse(Long courseId) {
        return completionRequirementRepository.findByCourseId(courseId);
    }

    // Method to create a new completion requirement for a course
    public CompletionRequirement createCompletionRequirement(Long courseId, CompletionRequirement completionRequirement) {
        Course course = new Course(); // You might want to load the course using courseId
        course.setId(courseId);
        completionRequirement.setCourse(course); // Set the course to the completion requirement
        
        return completionRequirementRepository.save(completionRequirement);
    }
}
