package com.example.lms.course.controller;

import com.example.lms.course.dto.CompletionRequirementDTO;
import com.example.lms.course.model.CompletionRequirement;
import com.example.lms.course.service.CompletionVerificationService;
import com.example.lms.course.assembler.CompletionRequirementAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/completion-requirements")
@Tag(name = "Completion Requirements", description = "APIs for managing course completion requirements")
public class CompletionRequirementController {

    @Autowired
    private CompletionVerificationService completionVerificationService;

    @Autowired
    private CompletionRequirementAssembler completionRequirementAssembler;

    /**
     * Get all completion requirements for a course
     * 
     * @param courseId The ID of the course
     * @return List of completion requirements for the specified course
     */
    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get all completion requirements for a course", 
               description = "Retrieves all completion requirements associated with the specified course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved completion requirements",
                     content = @Content(mediaType = "application/json", 
                                       schema = @Schema(implementation = CompletionRequirementDTO.class))),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or (hasRole('STUDENT') and #courseId == authentication.principal.course.id)")
    public ResponseEntity<List<CompletionRequirementDTO>> getCompletionRequirements(
            @Parameter(description = "ID of the course to retrieve completion requirements for", required = true)
            @PathVariable Long courseId) {
        List<CompletionRequirement> completionRequirements = completionVerificationService.getCompletionRequirementsForCourse(courseId);
        List<CompletionRequirementDTO> dtos = completionRequirementAssembler.toDTO(completionRequirements);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Create a new completion requirement for a course
     * 
     * @param courseId The ID of the course
     * @param completionRequirementDTO The completion requirement data
     * @return The created completion requirement
     */
    @PostMapping("/course/{courseId}")
    @Operation(summary = "Create a new completion requirement for a course", 
               description = "Creates a new completion requirement for the specified course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created completion requirement",
                     content = @Content(mediaType = "application/json", 
                                       schema = @Schema(implementation = CompletionRequirementDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<CompletionRequirementDTO> createCompletionRequirement(
            @Parameter(description = "ID of the course to create completion requirement for", required = true)
            @PathVariable Long courseId,
            @Parameter(description = "Completion requirement data", required = true)
            @RequestBody CompletionRequirementDTO completionRequirementDTO) {
        
        CompletionRequirement completionRequirement = completionRequirementAssembler.toEntity(completionRequirementDTO);
        CompletionRequirement createdRequirement = completionVerificationService.createCompletionRequirement(courseId, completionRequirement);
        CompletionRequirementDTO createdDTO = completionRequirementAssembler.toDTO(createdRequirement);
        
        return ResponseEntity.ok(createdDTO);
    }
    
    /**
     * Verify if a student has completed a course based on the completion requirements
     * 
     * @param studentId The ID of the student
     * @param courseId The ID of the course
     * @return Boolean indicating whether the student has completed the course
     */
    @GetMapping("/verify/{studentId}/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    @Operation(summary = "Verify course completion for a student", 
               description = "Checks if a student has met all the completion requirements for a course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully verified completion status",
                     content = @Content(mediaType = "application/json", 
                                       schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "404", description = "Student or course not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Boolean> verifyCompletion(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable Long studentId,
            @Parameter(description = "ID of the course", required = true)
            @PathVariable Long courseId) {
        boolean isCompleted = completionVerificationService.verifyCompletion(studentId, courseId);
        return ResponseEntity.ok(isCompleted);
    }
}