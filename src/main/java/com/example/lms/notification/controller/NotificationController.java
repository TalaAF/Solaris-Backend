package com.example.lms.notification.controller;

import com.example.lms.notification.dto.NotificationDTO;
import com.example.lms.notification.dto.NotificationPreferenceDTO;
import com.example.lms.notification.mapper.NotificationMapper;
import com.example.lms.notification.model.Notification;
import com.example.lms.notification.model.NotificationPreference;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.notification.service.NotificationPreferenceService;
import com.example.lms.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;
    private final NotificationMapper notificationMapper;
    
    /**
     * Get unread notifications for current user
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromEmail(userDetails);
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        List<NotificationDTO> dtos = notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Get unread notification count for current user
     */
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadNotificationCount(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromEmail(userDetails);
        long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Get all notifications for current user (paginated)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationDTO>> getAllNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserIdFromEmail(userDetails);
        Page<Notification> notifications = notificationService.getAllNotifications(userId, page, size);
        Page<NotificationDTO> dtos = notifications.map(notificationMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Mark a notification as read
     */
    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationDTO> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Notification notification = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(notificationMapper.toDTO(notification));
    }
    
    /**
     * Mark all notifications as read for current user
     */
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromEmail(userDetails);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Delete a notification
     */
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get notification preferences for current user
     */
    @GetMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationPreferenceDTO>> getPreferences(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromEmail(userDetails);
        List<NotificationPreference> preferences = preferenceService.getPreferences(userId);
        List<NotificationPreferenceDTO> dtos = preferences.stream()
                .map(notificationMapper::toPreferenceDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Update notification preference
     */
    @PutMapping("/preferences/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationPreferenceDTO> updatePreference(
            @PathVariable String type,
            @RequestParam boolean emailEnabled,
            @RequestParam boolean inAppEnabled,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromEmail(userDetails);
        NotificationType notificationType;
        
        try {
            notificationType = NotificationType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        
        NotificationPreference updated = preferenceService.updatePreference(
                userId, notificationType, emailEnabled, inAppEnabled);
        return ResponseEntity.ok(notificationMapper.toPreferenceDTO(updated));
    }
    
    /**
     * Update multiple notification preferences at once
     */
    @PutMapping("/preferences/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationPreferenceDTO>> updatePreferences(
            @RequestParam List<String> types,
            @RequestParam boolean emailEnabled,
            @RequestParam boolean inAppEnabled,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromEmail(userDetails);
        
        List<NotificationType> notificationTypes;
        try {
            notificationTypes = types.stream()
                    .map(NotificationType::valueOf)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        
        List<NotificationPreference> updated = preferenceService.updatePreferences(
                userId, notificationTypes, emailEnabled, inAppEnabled);
        List<NotificationPreferenceDTO> dtos = updated.stream()
                .map(notificationMapper::toPreferenceDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    // Helper method to extract user ID from UserDetails
    private Long getUserIdFromEmail(UserDetails userDetails) {
        // You need to implement this based on your user model and repository
        // For example:
        // return userRepository.findByEmail(userDetails.getUsername()).getId();
        return 1L; // Placeholder - replace with actual implementation
    }
}