package com.example.lms.assignment.assignments.controller;

import com.example.lms.assignment.assignments.model.Assignment;
import com.example.lms.assignment.assignments.repository.AssignmentRepository;
import com.example.lms.assignment.submission.model.Submission;
import com.example.lms.assignment.submission.repository.SubmissionRepository;
import com.example.lms.course.repository.CourseRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Assessment", description = "APIs for managing assignments")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Create a new assignment", description = "Creates a new assignment for a course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Assignment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
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
    @Operation(summary = "Get all assignments", description = "Retrieves a list of all assignments. Requires INSTRUCTOR or ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved assignments"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<Assignment>> getAllAssignments() {
        logger.info("Fetching all assignments");
        List<Assignment> assignments = assignmentRepository.findAll();
        logger.info("Found {} assignments", assignments.size());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get assignment by ID", description = "Retrieves a specific assignment by its unique identifier. Requires INSTRUCTOR or ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved assignments"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @Operation(
    summary = "Delete an assignment",
    description = "Deletes a specific assignment by its ID. Requires ADMIN or INSTRUCTOR role."
)
@ApiResponses({
    @ApiResponse(responseCode = "204", description = "Assignment deleted successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid assignment ID"),
    @ApiResponse(responseCode = "403", description = "Unauthorized access"),
    @ApiResponse(responseCode = "404", description = "Assignment not found")
})
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
   @Operation(
    summary = "Get submissions for assignment", 
    description = "Retrieves all submissions for a specific assignment. Requires INSTRUCTOR or ADMIN role."
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Submissions retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid assignment ID"),
    @ApiResponse(responseCode = "403", description = "Unauthorized access"),
    @ApiResponse(responseCode = "404", description = "Assignment not found")
})
    public ResponseEntity<List<Submission>> getSubmissionsForAssignment(@PathVariable Long assignmentId) {
    logger.info("Fetching submissions for assignmentId={}", assignmentId);
    assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + assignmentId));
    List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
    logger.info("Found {} submissions for assignmentId={}", submissions.size(), assignmentId);
    return ResponseEntity.ok(submissions);
}

}

