package com.example.lms.notification.model;

import com.example.lms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "notification_templates")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate extends BaseEntity {
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private NotificationType type;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(nullable = false)
    private String emailTemplateName;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String inAppTemplate;
    
    @Column(nullable = false)
    private boolean active = true;
}