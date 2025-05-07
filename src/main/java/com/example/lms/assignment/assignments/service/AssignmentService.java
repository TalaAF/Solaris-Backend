package com.example.lms.assignment.assignments.service;

import com.example.lms.assignment.assignments.dto.AssignmentDTO;
import com.example.lms.assignment.submission.dto.SubmissionDTO;
import com.example.lms.common.Exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AssignmentService {
    /**
     * Create a new assignment
     *
     * @param assignmentDTO Assignment data to create
     * @return Created assignment
     */
    AssignmentDTO createAssignment(AssignmentDTO assignmentDTO);
    
    /**
     * Get an assignment by ID
     *
     * @param id Assignment ID
     * @return Assignment with the given ID
     * @throws ResourceNotFoundException if assignment not found
     */
    AssignmentDTO getAssignmentById(Long id);
    
    /**
     * Get all assignments (with pagination, filtering, and search)
     *
     * @param pageable Pagination information
     * @param search Optional search term
     * @param courseId Optional course ID filter
     * @param published Optional published status filter
     * @return Page of assignments
     */
    Page<AssignmentDTO> getAllAssignments(Pageable pageable, String search, Long courseId, Boolean published);
    
    /**
     * Get all assignments for a course
     *
     * @param courseId Course ID
     * @return List of assignments for the course
     */
    List<AssignmentDTO> getAssignmentsByCourse(Long courseId);
    
    /**
     * Update an existing assignment
     *
     * @param id Assignment ID
     * @param assignmentDTO Updated assignment data
     * @return Updated assignment
     * @throws ResourceNotFoundException if assignment not found
     */
    AssignmentDTO updateAssignment(Long id, AssignmentDTO assignmentDTO);
    
    /**
     * Delete an assignment
     *
     * @param id Assignment ID
     * @throws ResourceNotFoundException if assignment not found
     */
    void deleteAssignment(Long id);
    
    /**
     * Publish an assignment (make it visible to students)
     *
     * @param id Assignment ID
     * @return Published assignment
     * @throws ResourceNotFoundException if assignment not found
     */
    AssignmentDTO publishAssignment(Long id);
    
    /**
     * Unpublish an assignment (hide it from students)
     *
     * @param id Assignment ID
     * @return Unpublished assignment
     * @throws ResourceNotFoundException if assignment not found
     */
    AssignmentDTO unpublishAssignment(Long id);
    
    /**
     * Get all submissions for an assignment
     *
     * @param assignmentId Assignment ID
     * @return List of submissions for the assignment
     */
    List<SubmissionDTO> getSubmissionsForAssignment(Long assignmentId);
}