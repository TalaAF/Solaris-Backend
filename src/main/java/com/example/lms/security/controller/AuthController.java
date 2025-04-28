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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API endpoints for user authentication and registration")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
})
public class AuthController {

    private final SimplifiedAuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final TokenStoreService tokenStoreService;
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
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
    @Operation(summary = "User registration", description = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
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
    
    @GetMapping("/me")
    @Operation(summary = "Get current user info", description = "Returns information about the currently logged in user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information retrieved successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.UserInfo.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create a UserInfo object using our nested class from AuthResponse
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
            user.getId(),
            user.getFullName(), // This will appear as "name" in the response
            user.getEmail(),
            user.getRoles(),
            user.getProfilePicture() // This will appear as "profileImage" in the response
        );
        
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh JWT token", description = "Refresh the access token using the refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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
    @Operation(summary = "User logout", description = "Logout user and invalidate refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")})
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
}