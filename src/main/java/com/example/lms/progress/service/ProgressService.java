package com.example.lms.progress.service;

import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.progress.dto.ProgressDTO;
import com.example.lms.progress.model.Progress;
import com.example.lms.progress.repository.ProgressRepository;
import com.example.lms.progress.assembler.ProgressAssembler;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ProgressAssembler progressAssembler;

    @Transactional
    public ProgressDTO updateProgress(Long studentId, Long courseId, Double progress) {
        validateProgress(progress);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        Progress existingProgress = progressRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(new Progress(student, course, 0.0, LocalDateTime.now()));

        // Implement max progress logic
        existingProgress.setProgress(Math.min(progress, 100.0));
        existingProgress.setLastUpdated(LocalDateTime.now());

        Progress savedProgress = progressRepository.save(existingProgress);
        return progressAssembler.toDTO(savedProgress);
    }

    @Transactional(readOnly = true)
    public ProgressDTO getProgress(Long studentId, Long courseId) {
        Progress existingProgress = progressRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found"));

        return progressAssembler.toDTO(existingProgress);
    }

    @Transactional(readOnly = true)
    public List<ProgressDTO> getAllProgressForStudent(Long studentId) {
        List<Progress> progressList = progressRepository.findByStudentId(studentId);
        
        return progressList.stream()
                .map(progressAssembler::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double calculateOverallProgress(Long studentId) {
        List<Progress> progressList = progressRepository.findByStudentId(studentId);
        
        if (progressList.isEmpty()) {
            return 0.0;
        }

        return progressList.stream()
                .mapToDouble(Progress::getProgress)
                .average()
                .orElse(0.0);
    }

    private void validateProgress(Double progress) {
        if (progress == null || progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
    }
}