package com.example.lms.security.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @GetMapping("/oauth-redirect")
    public ResponseEntity<Map<String, String>> getOAuthRedirectUrl() {
        log.info("OAuth redirect endpoint called");
        try {
            Map<String, String> response = new HashMap<>();
            response.put("redirectUrl", "/oauth2/authorization/google");
            log.info("Returning OAuth redirect URL: {}", response.get("redirectUrl"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in OAuth redirect endpoint: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}