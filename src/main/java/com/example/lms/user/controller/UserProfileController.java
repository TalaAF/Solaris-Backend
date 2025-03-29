package com.example.lms.user.controller;

import com.example.lms.user.dto.UserProfileDTO;
import com.example.lms.user.service.UserProfileService;
import com.example.lms.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Tag(name = "User Profiles", description = "API endpoints for managing user profiles")

public class UserProfileController {
    
    private final UserProfileService userProfileService;
    private final UserRepository userRepository;
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userRepository.findById(#userId).orElse(new com.example.lms.user.model.User()).getEmail()")
    @Operation(summary = "Get user profile by ID", 
               description = "Returns a user profile based on the user ID. Users can only access their own profile unless they have ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile found",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.Response.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    public ResponseEntity<UserProfileDTO.Response> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileService.getUserProfile(userId));
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get my profile", 
               description = "Returns the profile of the authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile found",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.Response.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    public ResponseEntity<UserProfileDTO.Response> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromEmail(userDetails.getUsername());
        return ResponseEntity.ok(userProfileService.getUserProfile(userId));
    }
    
    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userRepository.findById(#userId).orElse(new com.example.lms.user.model.User()).getEmail()")
    @Operation(summary = "Create user profile", 
               description = "Creates a new user profile. Requires ADMIN role or the user can create their own profile.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User profile created successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.Response.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "400", description = "Bad request, invalid input")
    })
    public ResponseEntity<UserProfileDTO.Response> createUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileDTO.Request request) {
        return new ResponseEntity<>(userProfileService.createUserProfile(userId, request), HttpStatus.CREATED);
    }
    
    @PostMapping("/me")
    @Operation(summary = "Create my profile", 
               description = "Creates a new profile for the authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User profile created successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.Response.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "400", description = "Bad request, invalid input")
    })
    public ResponseEntity<UserProfileDTO.Response> createMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileDTO.Request request) {
        Long userId = getUserIdFromEmail(userDetails.getUsername());
        return new ResponseEntity<>(userProfileService.createUserProfile(userId, request), HttpStatus.CREATED);
    }
    
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userRepository.findById(#userId).orElse(new com.example.lms.user.model.User()).getEmail()")
    @Operation(summary = "Update user profile", 
               description = "Updates an existing user profile. Requires ADMIN role or the user can update their own profile.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.Response.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    public ResponseEntity<UserProfileDTO.Response> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileDTO.Request request) {
        return ResponseEntity.ok(userProfileService.updateUserProfile(userId, request));
    }
    
    @PutMapping("/me")
    @Operation(summary = "Update my profile", 
               description = "Updates the profile of the authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.Response.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userRepository.findById(#userId).orElse(new com.example.lms.user.model.User()).getEmail()")
    public ResponseEntity<UserProfileDTO.Response> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileDTO.Request request) {
        Long userId = getUserIdFromEmail(userDetails.getUsername());
        return ResponseEntity.ok(userProfileService.updateUserProfile(userId, request));
    }
    
    @PostMapping(value = "/{userId}/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userRepository.findById(#userId).orElse(new com.example.lms.user.model.User()).getEmail()")
    @Operation(summary = "Upload user profile picture", 
               description = "Uploads a new profile picture for the user. Requires ADMIN role or the user can upload their own picture.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.Response.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    public ResponseEntity<UserProfileDTO.Response> uploadProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userProfileService.updateProfilePicture(userId, file));
    }
    

    @PostMapping(value = "/me/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload my profile picture", 
               description = "Uploads a new profile picture for the authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.Response.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userRepository.findById(#userId).orElse(new com.example.lms.user.model.User()).getEmail()")
    public ResponseEntity<UserProfileDTO.Response> uploadMyProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Long userId = getUserIdFromEmail(userDetails.getUsername());
        return ResponseEntity.ok(userProfileService.updateProfilePicture(userId, file));
    }
    
    // Helper method to extract user ID from email
    private Long getUserIdFromEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email))
                .getId();
    }
}