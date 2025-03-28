package com.example.lms.assignment.assessment.controller;

import com.example.lms.assignment.assignments.model.Assignment;
import com.example.lms.assignment.assignments.repository.AssignmentRepository;
import com.example.lms.assignment.submission.model.Submission;
import com.example.lms.assignment.submission.repository.SubmissionRepository;
import com.example.lms.course.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @PostMapping("/assignments")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Assignment> createAssignment(@RequestBody Assignment assignment) {
        logger.info("Creating assignment: title={}, courseId={}", assignment.getTitle(), assignment.getCourseId());

        // Validate the assignment
        if (assignment.getCourseId() == null || assignment.getTitle() == null || assignment.getMaxScore() == null) {
            logger.warn("Invalid assignment: Course ID, title, and max score are required");
            throw new IllegalArgumentException("Course ID, title, and max score are required");
        }
        if (assignment.getTitle().trim().isEmpty()) {
            logger.warn("Invalid assignment: Assignment title cannot be empty");
            throw new IllegalArgumentException("Assignment title cannot be empty");
        }
        if (assignment.getMaxScore() <= 0) {
            logger.warn("Invalid assignment: Max score must be greater than 0");
            throw new IllegalArgumentException("Max score must be greater than 0");
        }

        // Validate the courseId
        courseRepository.findById(assignment.getCourseId())
                .orElseThrow(() -> {
                    logger.error("Course not found with ID: {}", assignment.getCourseId());
                    return new IllegalArgumentException("Course not found with ID: " + assignment.getCourseId());
                });

        // Set a default due date if not provided
        if (assignment.getDueDate() == null) {
            assignment.setDueDate(LocalDateTime.now().plusDays(5)); // Default to 5 days from now
            logger.info("No due date provided, setting default due date: {}", assignment.getDueDate());
        }

        // Save the assignment
        Assignment savedAssignment = assignmentRepository.save(assignment);
        logger.info("Assignment created successfully: id={}, title={}", savedAssignment.getId(), savedAssignment.getTitle());
        return ResponseEntity.ok(savedAssignment);
    }

    @GetMapping("/assignments")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<List<Assignment>> getAllAssignments() {
        logger.info("Fetching all assignments");
        List<Assignment> assignments = assignmentRepository.findAll();
        logger.info("Found {} assignments", assignments.size());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Long id) {
        logger.info("Fetching assignment with id={}", id);
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Assignment not found with ID: {}", id);
                    return new IllegalArgumentException("Assignment not found with ID: " + id);
                });
        logger.info("Assignment found: id={}, title={}", assignment.getId(), assignment.getTitle());
        return ResponseEntity.ok(assignment);
    }

    @DeleteMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        logger.info("Deleting assignment with id={}", id);
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Assignment not found with ID: {}", id);
                    return new IllegalArgumentException("Assignment not found with ID: " + id);
                });
        assignmentRepository.delete(assignment);
        logger.info("Assignment deleted successfully: id={}", id);
        return ResponseEntity.noContent().build();
    }
    

    @GetMapping("/assignments/{assignmentId}/submissions")
@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
public ResponseEntity<List<Submission>> getSubmissionsForAssignment(@PathVariable Long assignmentId) {
    logger.info("Fetching submissions for assignmentId={}", assignmentId);
    assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + assignmentId));
    List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
    logger.info("Found {} submissions for assignmentId={}", submissions.size(), assignmentId);
    return ResponseEntity.ok(submissions);
}

}