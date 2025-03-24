package com.example.lms.user.controller;

import com.example.lms.user.dto.UserProfileDTO;
import com.example.lms.user.service.UserProfileService;
import com.example.lms.user.repository.UserRepository;
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
public class UserProfileController {
    
    private final UserProfileService userProfileService;
    private final UserRepository userRepository;
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userRepository.findById(#userId).orElse(new com.example.lms.user.model.User()).getEmail()")
    public ResponseEntity<UserProfileDTO.Response> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileService.getUserProfile(userId));
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO.Response> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromEmail(userDetails.getUsername());
        return ResponseEntity.ok(userProfileService.getUserProfile(userId));
    }
    
    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userRepository.findById(#userId).orElse(new com.example.lms.user.model.User()).getEmail()")
    public ResponseEntity<UserProfileDTO.Response> createUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileDTO.Request request) {
        return new ResponseEntity<>(userProfileService.createUserProfile(userId, request), HttpStatus.CREATED);
    }
    
    @PostMapping("/me")
    public ResponseEntity<UserProfileDTO.Response> createMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileDTO.Request request) {
        Long userId = getUserIdFromEmail(userDetails.getUsername());
        return new ResponseEntity<>(userProfileService.createUserProfile(userId, request), HttpStatus.CREATED);
    }
    
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userRepository.findById(#userId).orElse(new com.example.lms.user.model.User()).getEmail()")
    public ResponseEntity<UserProfileDTO.Response> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileDTO.Request request) {
        return ResponseEntity.ok(userProfileService.updateUserProfile(userId, request));
    }
    
    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO.Response> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileDTO.Request request) {
        Long userId = getUserIdFromEmail(userDetails.getUsername());
        return ResponseEntity.ok(userProfileService.updateUserProfile(userId, request));
    }
    
    @PostMapping(value = "/{userId}/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userRepository.findById(#userId).orElse(new com.example.lms.user.model.User()).getEmail()")
    public ResponseEntity<UserProfileDTO.Response> uploadProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userProfileService.updateProfilePicture(userId, file));
    }
    
    @PostMapping(value = "/me/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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