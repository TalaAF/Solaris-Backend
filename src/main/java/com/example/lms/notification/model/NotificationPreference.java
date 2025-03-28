package com.example.lms.notification.model;

import com.example.lms.common.BaseEntity;
import com.example.lms.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "notification_preferences", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "type"}))
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference extends BaseEntity {
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Column(nullable = false)
    private boolean emailEnabled = true;
    
    @Column(nullable = false)
    private boolean inAppEnabled = true;
}