package com.example.lms.progress.controller;

import com.example.lms.common.Exception.ErrorResponse;
import com.example.lms.progress.dto.ProgressDTO;
import com.example.lms.progress.service.ProgressService;
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

/**
 * REST API controller for student progress management
 */
@RestController
@RequestMapping("/api/progress")
@Tag(name = "Progress", description = "Operations for tracking overall course progress")
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    /**
     * Update progress for a student in a course
     *
     * @param studentId Student ID
     * @param courseId Course ID
     * @param progress Progress percentage (0-100)
     * @return Updated progress data
     */
    @PutMapping("/update/{studentId}/{courseId}")
    @Operation(
        summary = "Update progress for a student in a course",
        description = "Updates the progress percentage for a specific student in a specific course"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Progress updated successfully",
            content = @Content(schema = @Schema(implementation = ProgressDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid progress value",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student or course not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public ResponseEntity<ProgressDTO> updateProgress(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable Long studentId, 
            
            @Parameter(description = "ID of the course", required = true)
            @PathVariable Long courseId, 
            
            @Parameter(description = "Progress percentage (0-100)", required = true)
            @RequestParam Double progress) {
        
        ProgressDTO updatedProgress = progressService.updateProgress(studentId, courseId, progress);
        return ResponseEntity.ok(updatedProgress);
    }

    /**
     * Get progress for a student in a course
     *
     * @param studentId Student ID
     * @param courseId Course ID
     * @return Progress data
     */
    @GetMapping("/{studentId}/{courseId}")
    @Operation(
        summary = "Get progress for a student in a course",
        description = "Retrieves the progress percentage for a specific student in a specific course"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(schema = @Schema(implementation = ProgressDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student or course not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public ResponseEntity<ProgressDTO> getProgress(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable Long studentId, 
            
            @Parameter(description = "ID of the course", required = true)
            @PathVariable Long courseId) {
        
        ProgressDTO progress = progressService.getProgress(studentId, courseId);
        return ResponseEntity.ok(progress);
    }
}