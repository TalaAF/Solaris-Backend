package com.example.lms.progress.controller;

import com.example.lms.common.Exception.ErrorResponse;
import com.example.lms.progress.dto.CourseProgressVisualizationDTO;
import com.example.lms.progress.service.ProgressVisualizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API controller for progress visualization
 */
@RestController
@RequestMapping("/api/progress-visualization")
@Tag(name = "Progress Visualization", description = "Operations for visualizing progress data")
public class ProgressVisualizationController {

    private static final Logger logger = LoggerFactory.getLogger(ProgressVisualizationController.class);

    @Autowired
    private ProgressVisualizationService progressVisualizationService;

    /**
     * Health check endpoint for testing API connectivity
     */
    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Progress visualization API is operational");
    }

    /**
     * Get overall progress for a student across all courses
     * 
     * @param studentId Student ID
     * @return Overall progress percentage
     */
    @GetMapping("/overall/{studentId}")
    @Operation(
        summary = "Get overall progress for a student",
        description = "Calculates the overall progress percentage across all courses for a specific student"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(schema = @Schema(type = "number", format = "double", description = "Overall progress percentage (0-100)"))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public ResponseEntity<?> getOverallProgress(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable Long studentId) {
        
        try {
            logger.info("Fetching overall progress for student ID: {}", studentId);
            Double overallProgress = progressVisualizationService.calculateOverallProgress(studentId);
            logger.info("Returning overall progress for student ID {}: {}", studentId, overallProgress);
            return ResponseEntity.ok(overallProgress);
        } catch (Exception e) {
            logger.error("Error fetching overall progress for student ID: " + studentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder().message("Error fetching overall progress: " + e.getMessage()).build());
        }
    }
    
    /**
     * Get course progress visualization data for a student
     * 
     * @param studentId Student ID
     * @return CourseProgressVisualizationDTO with overall and course-specific progress
     */
    @GetMapping("/courses/{studentId}")
    @Operation(
        summary = "Get course progress visualization data for a student",
        description = "Returns overall completion and individual course progress data for visualization"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(schema = @Schema(implementation = CourseProgressVisualizationDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public ResponseEntity<?> getCourseProgressVisualization(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable Long studentId) {
        
        try {
            logger.info("Fetching course progress visualization for student ID: {}", studentId);
            CourseProgressVisualizationDTO visualization = progressVisualizationService.getProgressVisualization(studentId);
            logger.info("Retrieved course progress for student ID: {}, with {} courses", 
                    studentId, visualization.getCourses() != null ? visualization.getCourses().size() : 0);
            return ResponseEntity.ok(visualization);
        } catch (Exception e) {
            logger.error("Error fetching course progress visualization for student ID: " + studentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder().message("Error fetching course progress: " + e.getMessage()).build());
        }
    }
    
    /**
     * Get progress by category for a student
     * 
     * @param studentId Student ID
     * @return Map of categories to progress percentages
     */
    @GetMapping("/by-category/{studentId}")
    @Operation(
        summary = "Get progress by category for a student",
        description = "Calculates progress percentages grouped by course categories or departments"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(schema = @Schema(implementation = Object.class, description = "Map of categories to progress percentages"))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public ResponseEntity<?> getProgressByCategory(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable Long studentId) {
        
        try {
            logger.info("Fetching progress by category for student ID: {}", studentId);
            // This would be implemented in the service
            // For now, we'll return a placeholder
            return ResponseEntity.ok(java.util.Collections.singletonMap("status", "Not implemented yet"));
        } catch (Exception e) {
            logger.error("Error fetching progress by category for student ID: " + studentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder().message("Error fetching progress by category: " + e.getMessage()).build());
        }
    }
    
    /**
     * Get progress trend for a student
     * 
     * @param studentId Student ID
     * @param days Number of days to include in the trend
     * @return Map of dates to progress percentages
     */
    @GetMapping("/trend/{studentId}")
    @Operation(
        summary = "Get progress trend for a student",
        description = "Retrieves progress data over time to visualize trends"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(schema = @Schema(implementation = Object.class, description = "Map of dates to progress percentages"))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public ResponseEntity<?> getProgressTrend(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable Long studentId,
            
            @Parameter(description = "Number of days to include in the trend (default: 30)")
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        
        try {
            logger.info("Fetching progress trend for student ID: {} for the last {} days", studentId, days);
            // This would be implemented in the service
            // For now, we'll return a placeholder
            return ResponseEntity.ok(java.util.Collections.singletonMap("status", "Not implemented yet"));
        } catch (Exception e) {
            logger.error("Error fetching progress trend for student ID: " + studentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder().message("Error fetching progress trend: " + e.getMessage()).build());
        }
    }
}