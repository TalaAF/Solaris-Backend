package com.example.lms.security.controller;

import com.example.lms.security.dto.ForgotPasswordRequest;
import com.example.lms.security.dto.MessageResponse;
import com.example.lms.security.dto.ResetPasswordRequest;
import com.example.lms.security.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Password Management", description = "API endpoints for password management")
public class PasswordController {
    
    private final PasswordService passwordService;
    private final Environment env;  // Add Environment dependency
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot Password", description = "Process forgot password request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset link sent successfully",
                content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Processing forgot password request for email: {}", request.getEmail());
        
        try {
            // For security reasons, we don't reveal if the email exists in our system
            String resetToken = passwordService.processForgotPassword(request);
            
            MessageResponse response = new MessageResponse(
                "If the email exists in our system, a password reset link will be sent shortly");
            
            // Only in development - add token to response
            if (env.getActiveProfiles().length == 0 || 
                Arrays.asList(env.getActiveProfiles()).contains("dev")) {
                response.setDevToken(resetToken);
                response.setDevResetUrl("/reset-password?token=" + resetToken);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing forgot password request", e);
            // Still return a success message to avoid user enumeration
            return ResponseEntity.ok(new MessageResponse(
                "If the email exists in our system, a password reset link will be sent shortly"));
        }
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password", description = "Reset user password using token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully",
                content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Invalid or expired token")
    })
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Processing password reset request");
        
        passwordService.resetPassword(request);
        
        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully"));
    }
    
    @GetMapping("/validate-reset-token")
    @Operation(summary = "Validate Password Reset Token", description = "Validate the password reset token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid",
                content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<MessageResponse> validateResetToken(@RequestParam("token") String token) {
        log.info("Validating password reset token");
        
        boolean isValid = passwordService.validateToken(token);
        
        if (isValid) {
            return ResponseEntity.ok(new MessageResponse("Token is valid"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Token is invalid or expired"));
        }
    }
}