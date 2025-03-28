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
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import com.example.lms.security.token.service.TokenStoreService;
import com.example.lms.security.token.model.UserToken;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
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
    private final UserRepository userRepository;
    private final TokenStoreService tokenStoreService;
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        log.info("Login request received for: {}", loginRequest.getEmail());
        try {
            AuthResponse response = authService.login(loginRequest, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        log.info("Registration request received for: {}", registerRequest.getEmail());
        try {
            AuthResponse response = authService.register(registerRequest, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request,
        HttpServletRequest httpRequest) {
        log.info("Token refresh request received");
        
        String requestRefreshToken = request.getRefreshToken();
        try {
            return refreshTokenService.findByToken(requestRefreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> { 
                        // Pass the HttpServletRequest to generateToken
                        String token = tokenProvider.generateToken(
                                authService.createAuthenticationToken(user), 
                                httpRequest);
                        
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
            try {
        // Extract token ID from the JWT
        String jwt = logoutRequest.getRefreshToken();
        Claims claims = tokenProvider.parseToken(jwt);
        String tokenId = claims.get("tokenId", String.class);
        
        // Revoke the token in the token store
        tokenStoreService.revokeToken(tokenId);
        
        // Clear security context
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Log out successful"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Error during logout: " + e.getMessage()));
    }
     }
         @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth endpoint is working");
    }
}