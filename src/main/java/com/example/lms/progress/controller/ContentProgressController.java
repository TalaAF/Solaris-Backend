package com.example.lms.progress.controller;

import com.example.lms.common.Exception.ErrorResponse;
import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.progress.model.ContentProgress;
import com.example.lms.progress.service.ContentProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for content progress tracking
 */
@RestController
@RequestMapping("/api/content-progress")
@Tag(name = "Content Progress", description = "Operations for tracking progress on specific content items")
public class ContentProgressController {

    @Autowired
    private ContentProgressService contentProgressService;

    /**
     * Update progress for a student on specific content
     * 
     * @param studentId Student ID
     * @param contentId Content ID
     * @param progress Progress percentage (0-100)
     * @return Updated content progress
     */
    @PutMapping("/update/{studentId}/{contentId}")
    @Operation(
        summary = "Update content progress for a student",
        description = "Updates the progress percentage for a specific student on a specific content item"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Progress updated successfully",
            content = @Content(schema = @Schema(implementation = ContentProgress.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid progress value",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student, content, or enrollment not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public ResponseEntity<ContentProgress> updateProgress(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable Long studentId,
            
            @Parameter(description = "ID of the content", required = true)
            @PathVariable Long contentId,
            
            @Parameter(description = "Progress percentage (0-100)", required = true)
            @RequestParam Double progress) {

        // Check for valid progress range
        if (progress < 0 || progress > 100) {
            return ResponseEntity.badRequest().body(null); // Return 400 Bad Request
        }
        
        try {
            ContentProgress updatedProgress = contentProgressService.updateProgress(studentId, contentId, progress);
            return ResponseEntity.ok(updatedProgress); // Return 200 OK with updated content progress
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 Not Found if no progress found
        }
    }

    /**
     * Get all content progress for a student
     * 
     * @param studentId Student ID
     * @return List of content progress records
     */
    @GetMapping("/{studentId}")
    @Operation(
        summary = "Get content progress for a student",
        description = "Retrieves all content progress records for a specific student"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContentProgress.class)))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found or no progress records found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public ResponseEntity<List<ContentProgress>> getStudentProgress(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable Long studentId) {
        
        List<ContentProgress> progress = contentProgressService.getStudentProgress(studentId);
        
        if (progress.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 if no progress found
        }
        
        return ResponseEntity.ok(progress); // Return 200 OK with the list of content progress
    }
}