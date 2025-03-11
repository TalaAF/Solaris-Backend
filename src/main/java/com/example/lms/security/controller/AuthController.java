package com.example.lms.security.controller;

import com.example.lms.security.dto.AuthResponse;
import com.example.lms.security.dto.LoginRequest;
import com.example.lms.security.dto.LogoutRequest;
import com.example.lms.security.dto.MessageResponse;
import com.example.lms.security.dto.RegisterRequest;
import com.example.lms.security.dto.TokenRefreshRequest;
import com.example.lms.security.dto.TokenRefreshResponse;
import com.example.lms.security.exception.TokenRefreshException;
import com.example.lms.security.jwt.JwtTokenProvider;
import com.example.lms.security.model.RefreshToken;
import com.example.lms.security.service.RefreshTokenService;
import com.example.lms.security.service.SimplifiedAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final SimplifiedAuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for: {}", loginRequest.getEmail());
        try {
            AuthResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration request received for: {}", registerRequest.getEmail());
        try {
            AuthResponse response = authService.register(registerRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Token refresh request received");
        
        String requestRefreshToken = request.getRefreshToken();
        try {
            return refreshTokenService.findByToken(requestRefreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        String token = tokenProvider.generateToken(
                                authService.createAuthenticationToken(user));
                        
                        log.info("Generated new access token for user: {}", user.getEmail());
                        return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                    })
                    .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found"));
        } catch (TokenRefreshException e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw e;
        }
     }
    
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logoutUser(@Valid @RequestBody LogoutRequest logoutRequest) {
        log.info("Logout request received");
        
         try {
            refreshTokenService.revokeToken(logoutRequest.getRefreshToken());
            SecurityContextHolder.clearContext();
            
            return ResponseEntity.ok(new MessageResponse("Log out successful"));
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error during logout: " + e.getMessage()));
        }
     }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth endpoint is working");
    }
}