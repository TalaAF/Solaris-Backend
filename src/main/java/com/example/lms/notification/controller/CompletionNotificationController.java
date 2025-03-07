package com.example.lms.notification.controller;

import com.example.lms.notification.dto.CompletionNotificationDTO;
import com.example.lms.notification.service.CompletionNotificationService;
import com.example.lms.notification.assembler.CompletionNotificationAssembler;
import com.example.lms.notification.model.CompletionNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class CompletionNotificationController {

    @Autowired
    private CompletionNotificationService notificationService;

    @Autowired
    private CompletionNotificationAssembler assembler;

    @GetMapping("/{studentId}")
    public ResponseEntity<List<CompletionNotificationDTO>> getNotifications(@PathVariable Long studentId) {
        List<CompletionNotification> notifications = notificationService.getNotificationsForStudent(studentId);
        return ResponseEntity.ok(assembler.toDTO(notifications));
    }
}
