package com.example.lms.notification.service;

import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.notification.model.NotificationPreference;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.notification.repository.NotificationPreferenceRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {
    
    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    
    /**
     * Get all preferences for a user
     */
    @Transactional
    public List<NotificationPreference> getPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<NotificationPreference> preferences = preferenceRepository.findByUser(user);
        
        // Ensure all notification types have preferences
        if (preferences.size() < NotificationType.values().length) {
            createDefaultPreferencesIfNeeded(user);
            preferences = preferenceRepository.findByUser(user);
        }
        
        return preferences;
    }
    
    /**
     * Get preference for a specific notification type
     */
    @Transactional(readOnly = true)
    public NotificationPreference getPreference(Long userId, NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return preferenceRepository.findByUserAndType(user, type)
                .orElseGet(() -> createDefaultPreference(user, type));
    }
    
    /**
     * Get or create preference for a specific notification type
     */
    @Transactional
    public NotificationPreference getOrCreatePreference(Long userId, NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return preferenceRepository.findByUserAndType(user, type)
                .orElseGet(() -> createDefaultPreference(user, type));
    }
    
    /**
     * Update user preference for a notification type
     */
    @Transactional
    public NotificationPreference updatePreference(Long userId, NotificationType type, 
                                                  boolean emailEnabled, boolean inAppEnabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        NotificationPreference preference = preferenceRepository.findByUserAndType(user, type)
                .orElseGet(() -> NotificationPreference.builder()
                        .user(user)
                        .type(type)
                        .build());
        
        preference.setEmailEnabled(emailEnabled);
        preference.setInAppEnabled(inAppEnabled);
        
        return preferenceRepository.save(preference);
    }
    
    /**
     * Update multiple preferences at once
     */
    @Transactional
    public List<NotificationPreference> updatePreferences(Long userId, 
                                                        List<NotificationType> types,
                                                        boolean emailEnabled, 
                                                        boolean inAppEnabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<NotificationPreference> updatedPreferences = new ArrayList<>();
        
        for (NotificationType type : types) {
            NotificationPreference preference = preferenceRepository.findByUserAndType(user, type)
                    .orElseGet(() -> NotificationPreference.builder()
                            .user(user)
                            .type(type)
                            .build());
            
            preference.setEmailEnabled(emailEnabled);
            preference.setInAppEnabled(inAppEnabled);
            
            updatedPreferences.add(preference);
        }
        
        return preferenceRepository.saveAll(updatedPreferences);
    }
    
    /**
     * Create default preferences for all notification types
     */
    @Transactional
    public void createDefaultPreferencesIfNeeded(User user) {
        List<NotificationPreference> existing = preferenceRepository.findByUser(user);
        Set<NotificationType> existingTypes = existing.stream()
                .map(NotificationPreference::getType)
                .collect(Collectors.toSet());
        
        List<NotificationPreference> toCreate = new ArrayList<>();
        
        for (NotificationType type : NotificationType.values()) {
            if (!existingTypes.contains(type)) {
                toCreate.add(NotificationPreference.builder()
                        .user(user)
                        .type(type)
                        .emailEnabled(true)
                        .inAppEnabled(true)
                        .build());
            }
        }
        
        if (!toCreate.isEmpty()) {
            preferenceRepository.saveAll(toCreate);
            log.info("Created {} default notification preferences for user ID: {}", 
                    toCreate.size(), user.getId());
        }
    }
    
    /**
     * Create a default preference for a specific notification type
     */
    @Transactional
    public NotificationPreference createDefaultPreference(User user, NotificationType type) {
        NotificationPreference preference = NotificationPreference.builder()
                .user(user)
                .type(type)
                .emailEnabled(true)
                .inAppEnabled(true)
                .build();
        
        NotificationPreference saved = preferenceRepository.save(preference);
        log.info("Created default notification preference for user ID: {} and type: {}", 
                user.getId(), type);
        
        return saved;
    }
}