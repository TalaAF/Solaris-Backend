package com.example.lms.assignment.assignments.service;

import com.example.lms.assignment.assignments.dto.AssignmentDTO;
import com.example.lms.assignment.assignments.model.Assignment;
import com.example.lms.assignment.assignments.repository.AssignmentRepository;
import com.example.lms.assignment.submission.dto.SubmissionDTO;
import com.example.lms.assignment.submission.model.Submission;
import com.example.lms.assignment.submission.repository.SubmissionRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    @Transactional
    public AssignmentDTO createAssignment(AssignmentDTO assignmentDTO) {
        // Check if the course exists
        Course course = courseRepository.findById(assignmentDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + assignmentDTO.getCourseId()));
        
        // Create the assignment entity
        Assignment assignment = Assignment.builder()
                .title(assignmentDTO.getTitle())
                .description(assignmentDTO.getDescription())
                .dueDate(assignmentDTO.getDueDate())
                .maxScore(assignmentDTO.getMaxScore())
                .published(assignmentDTO.isPublished())
                .courseId(assignmentDTO.getCourseId())
                .build();
        
        // Save the assignment
        Assignment savedAssignment = assignmentRepository.save(assignment);
        
        // Map to response DTO
        return mapToDTO(savedAssignment, course.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentDTO getAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        
        // Get course name
        Course course = courseRepository.findById(assignment.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + assignment.getCourseId()));
        
        return mapToDTO(assignment, course.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentDTO> getAllAssignments(Pageable pageable, String search, Long courseId, Boolean published) {
        Page<Assignment> assignmentsPage;
        
        // Apply filters based on provided parameters
        if (search != null && !search.isEmpty() && courseId != null) {
            assignmentsPage = assignmentRepository.searchAssignmentsByCourse(search, courseId, pageable);
        } else if (search != null && !search.isEmpty()) {
            assignmentsPage = assignmentRepository.searchAssignments(search, pageable);
        } else if (courseId != null && published != null) {
            assignmentsPage = assignmentRepository.findByCourseIdAndPublished(courseId, published, pageable);
        } else if (courseId != null) {
            assignmentsPage = assignmentRepository.findByCourseId(courseId, pageable);
        } else if (published != null) {
            assignmentsPage = assignmentRepository.findByPublished(published, pageable);
        } else {
            assignmentsPage = assignmentRepository.findAll(pageable);
        }
        
        // Extract all unique course IDs
        List<Long> courseIds = assignmentsPage.getContent().stream()
                .map(Assignment::getCourseId)
                .distinct()
                .collect(Collectors.toList());
        
        // Fetch all courses in one query
        Map<Long, String> courseNames = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getTitle));
        
        // Map to DTOs using the preloaded course names
        return assignmentsPage.map(assignment -> 
            mapToDTO(assignment, courseNames.getOrDefault(assignment.getCourseId(), "Unknown Course"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentDTO> getAssignmentsByCourse(Long courseId) {
        // Check if course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        
        Course course = courseRepository.findById(courseId).orElse(null);
        String courseName = course != null ? course.getTitle() : "Unknown Course";
        
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        return assignments.stream()
                .map(assignment -> mapToDTO(assignment, courseName))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AssignmentDTO updateAssignment(Long id, AssignmentDTO assignmentDTO) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        
        // Check if the course exists
        Course course = courseRepository.findById(assignmentDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + assignmentDTO.getCourseId()));
        
        // Update the assignment
        assignment.setTitle(assignmentDTO.getTitle());
        assignment.setDescription(assignmentDTO.getDescription());
        assignment.setDueDate(assignmentDTO.getDueDate());
        assignment.setMaxScore(assignmentDTO.getMaxScore());
        assignment.setPublished(assignmentDTO.isPublished());
        assignment.setCourseId(assignmentDTO.getCourseId());
        
        // Save the updated assignment
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        
        return mapToDTO(updatedAssignment, course.getTitle());
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Assignment not found with id: " + id);
        }
        
        assignmentRepository.deleteById(id);
    }

    private AssignmentDTO togglePublishStatus(Long id, boolean publishStatus) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        
        assignment.setPublished(publishStatus);
        Assignment savedAssignment = assignmentRepository.save(assignment);
        
        Course course = courseRepository.findById(assignment.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + assignment.getCourseId()));
        
        return mapToDTO(savedAssignment, course.getTitle());
    }

    @Override
    @Transactional
    public AssignmentDTO publishAssignment(Long id) {
        return togglePublishStatus(id, true);
    }

    @Override
    @Transactional
    public AssignmentDTO unpublishAssignment(Long id) {
        return togglePublishStatus(id, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionDTO> getSubmissionsForAssignment(Long assignmentId) {
        // Check if assignment exists
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("Assignment not found with id: " + assignmentId);
        }
        
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        return submissions.stream()
                .map(this::mapToSubmissionDTO)
                .collect(Collectors.toList());
    }
    
    // Helper method to map Assignment entity to AssignmentDTO
    private AssignmentDTO mapToDTO(Assignment assignment, String courseName) {
        int submissionCount = submissionRepository.countByAssignmentId(assignment.getId()).intValue();
        
        return AssignmentDTO.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .courseId(assignment.getCourseId())
                .courseName(courseName)
                .dueDate(assignment.getDueDate())
                .maxScore(assignment.getMaxScore())
                .published(assignment.isPublished())
                .submissionCount(submissionCount)
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .createdBy(assignment.getCreatedAt().toString()) // Temporary fix: using date as string since getCreatedBy() doesn't exist
                .build();
    }
    
    // Helper method to map Submission entity to SubmissionDTO
    private SubmissionDTO mapToSubmissionDTO(Submission submission) {
        return SubmissionDTO.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignmentId())
                .userId(submission.getStudentId())
                .submittedAt(submission.getSubmissionDate())
                .grade(submission.getGrade() != null ? submission.getGrade().doubleValue() : null) // Convert Integer to Double
                .feedback(submission.getFeedback())
                .build();
    }
}