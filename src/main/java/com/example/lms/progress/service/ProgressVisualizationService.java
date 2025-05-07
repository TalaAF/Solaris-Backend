package com.example.lms.progress.service;

import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.progress.dto.CourseProgressVisualizationDTO;
import com.example.lms.progress.dto.CourseProgressVisualizationDTO.ProgressItemDTO;
import com.example.lms.progress.model.Progress;
import com.example.lms.progress.repository.ProgressRepository;
import com.example.lms.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProgressVisualizationService {

    @Autowired
    private ProgressRepository progressRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;

    public Double calculateOverallProgress(Long studentId) {
        // Check if student exists
        if (!userRepository.existsById(studentId)) {
            return 0.0;
        }
        
        // Get average progress from progress repository
        Double avgProgress = progressRepository.calculateAverageProgressForStudent(studentId);
        
        // Handle null case (when no courses are found)
        return avgProgress != null ? avgProgress : 0.0;
    }
    
    public CourseProgressVisualizationDTO getProgressVisualization(Long studentId) {
        // Check if student exists
        if (!userRepository.existsById(studentId)) {
            return createEmptyProgressVisualization();
        }
        
        // Get overall progress
        Double overallProgress = calculateOverallProgress(studentId);
        
        // Get all courses
        List<Course> courses = courseRepository.findAll();
        
        // Get progress for each course
        List<ProgressItemDTO> courseProgressList = new ArrayList<>();
        
        for (Course course : courses) {
            Optional<Progress> progressOpt = progressRepository.findByStudentIdAndCourseId(studentId, course.getId());
            Double percentage = progressOpt.map(Progress::getProgress).orElse(0.0);
            
            courseProgressList.add(ProgressItemDTO.builder()
                .name(course.getTitle())
                .percentage(percentage)
                .build());
        }
        
        // Build and return the DTO
        return CourseProgressVisualizationDTO.builder()
            .overall(ProgressItemDTO.builder()
                .name("Overall Completion")
                .percentage(overallProgress)
                .build())
            .courses(courseProgressList)
            .build();
    }
    
    private CourseProgressVisualizationDTO createEmptyProgressVisualization() {
        return CourseProgressVisualizationDTO.builder()
            .overall(ProgressItemDTO.builder()
                .name("Overall Completion")
                .percentage(0.0)
                .build())
            .courses(new ArrayList<>())
            .build();
    }
}