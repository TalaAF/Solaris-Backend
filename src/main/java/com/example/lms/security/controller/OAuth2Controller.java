package com.example.lms.security.controller;

import com.example.lms.security.dto.AuthResponse;
import com.example.lms.security.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {
    
    private final OAuth2Service oAuth2Service;
    
    @GetMapping("/oauth2/redirect/google")
    public ResponseEntity<Map<String, String>> getOAuth2RedirectUrl() {
        log.info("Getting OAuth2 redirect URL for Google");
        String redirectUrl = "/oauth2/authorization/google";
        return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
    }
    
    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> handleOAuth2Success(HttpServletRequest request, HttpServletResponse response) {
        log.info("Processing OAuth2 success callback");
        AuthResponse authResponse = oAuth2Service.processOAuth2Success(request);
        return ResponseEntity.ok(authResponse);
    }
    
    @GetMapping("/oauth2/error")
    public ResponseEntity<Map<String, String>> handleOAuth2Error() {
        log.error("OAuth2 authentication failed");
        return ResponseEntity.status(401).body(Map.of("error", "Authentication failed"));
    }
}