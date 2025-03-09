package com.example.lms.user.service;

import com.example.lms.common.Exception.ResourceAlreadyExistsException;
import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.user.dto.UserProfileDTO;
import com.example.lms.user.model.User;
import com.example.lms.user.model.UserProfile;
import com.example.lms.user.repository.UserProfileRepository;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserFileStorageService fileStorageService;
    
    @Transactional(readOnly = true)
    public UserProfileDTO.Response getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user with id: " + userId));
        
        return mapToResponseDTO(profile, user);
    }
    
    @Transactional
    public UserProfileDTO.Response createUserProfile(Long userId, UserProfileDTO.Request request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        if (userProfileRepository.existsByUser(user)) {
            throw new ResourceAlreadyExistsException("Profile already exists for user with id: " + userId);
        }
        
        UserProfile profile = mapToEntity(request);
        profile.setUser(user);
        
        UserProfile savedProfile = userProfileRepository.save(profile);
        return mapToResponseDTO(savedProfile, user);
    }
    
    @Transactional
    public UserProfileDTO.Response updateUserProfile(Long userId, UserProfileDTO.Request request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user with id: " + userId));
        
        updateEntityFromDTO(profile, request);
        
        UserProfile updatedProfile = userProfileRepository.save(profile);
        return mapToResponseDTO(updatedProfile, user);
    }
    
    @Transactional
    public UserProfileDTO.Response updateProfilePicture(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user with id: " + userId));
        
        // Delete old profile picture if it exists
        if (profile.getProfilePictureUrl() != null && !profile.getProfilePictureUrl().isBlank()) {
            fileStorageService.deleteProfilePicture(profile.getProfilePictureUrl());
        }
        
        // Store new profile picture
        String profilePictureUrl = fileStorageService.storeProfilePicture(file, userId);
        profile.setProfilePictureUrl(profilePictureUrl);
        
        UserProfile updatedProfile = userProfileRepository.save(profile);
        return mapToResponseDTO(updatedProfile, user);
    }
    
    private UserProfile mapToEntity(UserProfileDTO.Request request) {
        return UserProfile.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .biography(request.getBiography())
                .build();
    }
    
    private void updateEntityFromDTO(UserProfile profile, UserProfileDTO.Request request) {
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setBiography(request.getBiography());
    }
    
    private UserProfileDTO.Response mapToResponseDTO(UserProfile profile, User user) {
        return UserProfileDTO.Response.builder()
                .id(profile.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phoneNumber(profile.getPhoneNumber())
                .biography(profile.getBiography())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .isProfileComplete(profile.isProfileComplete())
                .build();
    }
}