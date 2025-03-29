package com.example.lms.common.Exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response model for API error responses.
 * This class is used for Swagger documentation and consistent error
 * response formatting across the API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response format")
public class ErrorResponse {
    
    @Schema(description = "Error message describing what went wrong", example = "Resource not found")
    private String message;
    
    @Schema(description = "HTTP status code", example = "404")
    private int status;
    
    @Schema(description = "HTTP status text", example = "NOT_FOUND")
    private String error;
    
    @Schema(description = "Path of the request that caused the error", example = "/api/progress/123/456")
    private String path;
    
    @Schema(description = "Timestamp when the error occurred")
    private LocalDateTime timestamp;
}