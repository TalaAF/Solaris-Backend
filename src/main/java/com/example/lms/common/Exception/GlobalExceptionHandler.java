package com.example.lms.common.Exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.lms.security.exception.TokenRefreshException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * 
 * This class provides centralized exception handling across the entire
 * application.
 * It translates various exceptions into appropriate HTTP responses with
 * meaningful error messages.
 * 
 * Key benefits:
 * 1. Consistent error responses across the API
 * 2. Proper HTTP status codes for different error types
 * 3. Helpful error messages for API consumers
 * 4. Prevents sensitive information leakage in error responses
 * 
 * This is a critical component for proper API error handling and security.
 */
@ControllerAdvice // Spring annotation for global exception handling
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, String>> handleDisabledException(DisabledException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, String>> handleLockedException(LockedException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
/**
     * Handles token refresh exceptions
     */
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<Map<String, String>> handleTokenRefreshException(TokenRefreshException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * Handles JWT token expired exceptions
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, String>> handleExpiredJwtException(ExpiredJwtException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "JWT token expired");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Handles invalid JWT token exceptions
     */
    @ExceptionHandler({MalformedJwtException.class, SignatureException.class, UnsupportedJwtException.class})
    public ResponseEntity<Map<String, String>> handleInvalidJwtException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Invalid JWT token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Handles validation errors from @Valid annotations
     * Maps field-specific validation errors to their error messages
     * 
     * @param ex The validation exception
     * @return 400 Bad Request with field-specific error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        // Extract field-specific errors from the exception
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors); // 400 Bad Request
    }



    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> errors = new HashMap<>();

        // Check if it's an enum validation error
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) ex.getCause();
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                String fieldName = ife.getPath().isEmpty() ? "unknown"
                        : ife.getPath().get(ife.getPath().size() - 1).getFieldName();

                String enumValues = ife.getTargetType().toString();
                // Extract enum values to show in the error message
                try {
                    Object[] enumConstants = ife.getTargetType().getEnumConstants();
                    StringBuilder validValues = new StringBuilder();
                    for (int i = 0; i < enumConstants.length; i++) {
                        validValues.append(enumConstants[i]);
                        if (i < enumConstants.length - 1) {
                            validValues.append(", ");
                        }
                    }
                    enumValues = validValues.toString();
                } catch (Exception e) {
                    // Fallback if we can't extract the enum values
                }

                errors.put(fieldName, "Invalid value. Acceptable values are: " + enumValues);
            } else {
                errors.put("error", "Invalid value in request body");
            }
        } else {
            errors.put("error", "Invalid request body format");
        }

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles entity not found exceptions
     * 
     * @param ex The not found exception
     * @return 404 Not Found with error message
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse); // 404 Not Found
    }

    /**
     * Handles illegal argument exceptions (e.g., validation errors not caught
     * by @Valid)
     * 
     * @param ex The illegal argument exception
     * @return 400 Bad Request with error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse); // 400 Bad Request
    }
 

    /**
     * Handles access denied exceptions from security checks
     * 
     * @param ex The access denied exception
     * @return 403 Forbidden with error message
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Access denied: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse); // 403 Forbidden
    }

    /**
     * Handles authentication failures
     * 
     * @param ex The bad credentials exception
     * @return 401 Unauthorized with generic error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        // Security: Use generic error message to prevent username enumeration
        errorResponse.put("message", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse); // 401 Unauthorized
    }

    /**
     * Catch-all handler for any unhandled exceptions
     * 
     * @param ex The uncaught exception
     * @return 500 Internal Server Error with generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "An error occurred: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // 500 Internal Server Error
    }
  
   @ExceptionHandler(ResourceNotFoundException.class) // Handle ResourceNotFoundException
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // Return 404 for resource not found
    }
  
}
