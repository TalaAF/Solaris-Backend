package com.example.lms.notification.model;

import com.example.lms.common.BaseEntity;
import com.example.lms.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;


import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    @Column(name = "category")
    private String category;
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private boolean read = false;
    
    @Column(nullable = false)
    private boolean sent = false;
    
    @Column(nullable = false)
    private boolean emailSent = false;
    
    @Column
    private LocalDateTime readAt;
    
    @Column
    private LocalDateTime sentAt;
    
    @Column
    private LocalDateTime emailSentAt;
    
    @Column(nullable = false)
    private int priority = 1; // 1-5 scale, 5 being highest
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId; // ID of related entity (course, assignment, etc.)
    
    @Column(name = "related_entity_type")
    private String relatedEntityType; // Type of related entity
}