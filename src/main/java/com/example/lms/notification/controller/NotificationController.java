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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "API endpoints for managing notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;
    private final NotificationMapper notificationMapper;
    
    /**
     * Get notifications by category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get notifications by category", description = "Retrieve notifications for the current user filtered by category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
                    content = @Content(schema = @Schema(implementation = NotificationDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires authentication")
    })
    public ResponseEntity<List<NotificationDTO>> getNotificationsByCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String category) {
        Long userId = getUserIdFromEmail(userDetails);
        List<Notification> notifications = notificationService.getNotificationsByCategory(userId, category);
        List<NotificationDTO> dtos = notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Get unread notification count by category
     */
    @GetMapping("/unread/count/{category}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread notification count by category", description = "Retrieve the count of unread notifications for the current user by category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread notification count retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires authentication")
    })
    public ResponseEntity<Long> getUnreadNotificationCountByCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String category) {
        Long userId = getUserIdFromEmail(userDetails);
        long count = notificationService.getUnreadNotificationCountByCategory(userId, category);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Mark all notifications as read for current user in a specific category
     */
    @PatchMapping("/read-all/category/{category}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all notifications as read in a category", description = "Mark all notifications as read for the current user in a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All notifications in the category marked as read",
                    content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires authentication")
    })
    public ResponseEntity<Void> markAllAsReadInCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String category) {
        Long userId = getUserIdFromEmail(userDetails);
        notificationService.markAllAsReadInCategory(userId, category);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get unread notifications for current user
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread notifications", description = "Retrieve unread notifications for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully",
                    content = @Content(schema = @Schema(implementation = NotificationDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires authentication")
    })
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
    @Operation(summary = "Get unread notification count", description = "Retrieve the count of unread notifications for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread notification count retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires authentication")
    })
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
    @Operation(summary = "Get all notifications", description = "Retrieve all notifications for the current user (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All notifications retrieved successfully",
                    content = @Content(schema = @Schema(implementation = NotificationDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires authentication")
    })
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
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read",
                    content = @Content(schema = @Schema(implementation = NotificationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires authentication")
    })
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
    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications as read for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All notifications marked as read",
                    content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires authentication")
    })
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
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification deleted successfully",
                    content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires authentication")
    })
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get notification preferences for current user
     */
    @GetMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get notification preferences", description = "Retrieve notification preferences for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification preferences retrieved successfully",
                    content = @Content(schema = @Schema(implementation = NotificationPreferenceDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires authentication")
    })
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
    @Operation(summary = "Update notification preference", description = "Update a specific notification preference")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification preference updated successfully",
                    content = @Content(schema = @Schema(implementation = NotificationPreferenceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires authentication")
    })
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
    @Operation(summary = "Update multiple notification preferences", description = "Update multiple notification preferences at once")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification preferences updated successfully",
                    content = @Content(schema = @Schema(implementation = NotificationPreferenceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden, requires authentication")
    })
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