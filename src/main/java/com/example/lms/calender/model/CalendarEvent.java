// src/main/java/com/example/lms/calender/model/CalendarEvent.java
package com.example.lms.calender.model;

import com.example.lms.common.BaseEntity;
import com.example.lms.user.model.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "calendar_events")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEvent extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column
    private String location;

    @Column
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType type;
    
    // User who created the event
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // External calendar integration identifier (optional)
    @Column
    private String externalEventId;
    
    // Flag for shared events (visible to all users with same role)
    @Column
    private boolean shared = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "audience")
    private EventAudience audience;
}