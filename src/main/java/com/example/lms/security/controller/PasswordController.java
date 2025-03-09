package com.example.lms.security.controller;

import com.example.lms.security.dto.ForgotPasswordRequest;
import com.example.lms.security.dto.MessageResponse;
import com.example.lms.security.dto.ResetPasswordRequest;
import com.example.lms.security.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class PasswordController {
    
    private final PasswordService passwordService;
    
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Processing forgot password request for email: {}", request.getEmail());
        
        try {
            // For security reasons, we don't reveal if the email exists in our system
            passwordService.processForgotPassword(request);
            return ResponseEntity.ok(new MessageResponse(
                "If the email exists in our system, a password reset link will be sent shortly"));
        } catch (Exception e) {
            log.error("Error processing forgot password request", e);
            // Still return a success message to avoid user enumeration
            return ResponseEntity.ok(new MessageResponse(
                "If the email exists in our system, a password reset link will be sent shortly"));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Processing password reset request");
        
        passwordService.resetPassword(request);
        
        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully"));
    }
    
    @GetMapping("/validate-reset-token")
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