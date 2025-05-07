package com.example.lms.assignment.assignments.controller;

import com.example.lms.assignment.assignments.dto.AssignmentDTO;
import com.example.lms.assignment.assignments.service.AssignmentService;
import com.example.lms.assignment.submission.dto.SubmissionDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignment Management", description = "API endpoints for managing assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Create a new assignment", description = "Create a new assignment with the specified details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Assignment created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    public ResponseEntity<AssignmentDTO> createAssignment(@Valid @RequestBody AssignmentDTO assignmentDTO) {
        AssignmentDTO createdAssignment = assignmentService.createAssignment(assignmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get all assignments", description = "Retrieve a paginated list of assignments with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved assignments")
    })
    public ResponseEntity<Page<AssignmentDTO>> getAllAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Boolean published,
            @RequestParam(defaultValue = "dueDate") String sort) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<AssignmentDTO> assignments = assignmentService.getAllAssignments(pageable, search, courseId, published);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @Operation(summary = "Get assignment by ID", description = "Retrieve a specific assignment by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved assignment"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    public ResponseEntity<AssignmentDTO> getAssignmentById(@PathVariable Long id) {
        AssignmentDTO assignment = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @Operation(summary = "Get assignments by course", description = "Retrieve all assignments for a specific course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved assignments"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsByCourse(@PathVariable Long courseId) {
        List<AssignmentDTO> assignments = assignmentService.getAssignmentsByCourse(courseId);
        return ResponseEntity.ok(assignments);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Update an assignment", description = "Update an existing assignment with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment updated successfully"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    public ResponseEntity<AssignmentDTO> updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentDTO assignmentDTO) {
        AssignmentDTO updatedAssignment = assignmentService.updateAssignment(id, assignmentDTO);
        return ResponseEntity.ok(updatedAssignment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Delete an assignment", description = "Delete an assignment from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Assignment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Publish assignment", description = "Make an assignment visible to students")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment published successfully"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    public ResponseEntity<AssignmentDTO> publishAssignment(@PathVariable Long id) {
        AssignmentDTO publishedAssignment = assignmentService.publishAssignment(id);
        return ResponseEntity.ok(publishedAssignment);
    }

    @PatchMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Unpublish assignment", description = "Hide an assignment from students")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment unpublished successfully"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    public ResponseEntity<AssignmentDTO> unpublishAssignment(@PathVariable Long id) {
        AssignmentDTO unpublishedAssignment = assignmentService.unpublishAssignment(id);
        return ResponseEntity.ok(unpublishedAssignment);
    }

    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Get submissions for assignment", description = "Retrieve all submissions for a specific assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved submissions"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    public ResponseEntity<List<SubmissionDTO>> getSubmissionsForAssignment(@PathVariable Long id) {
        List<SubmissionDTO> submissions = assignmentService.getSubmissionsForAssignment(id);
        return ResponseEntity.ok(submissions);
    }
}

