// src/main/java/com/example/lms/calendar/model/CalendarSettings.java
package com.example.lms.calender.model;

import com.example.lms.common.BaseEntity;
import com.example.lms.user.model.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "calendar_settings")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarSettings extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String externalCalendarId;

    @Column
    private String externalCalendarToken;
    
    @Column
    private String externalCalendarType; // GOOGLE, OUTLOOK, etc.
    
    @Column
    private boolean syncEnabled = false;
    
    // Calendar visibility preferences
    @Column
    private boolean showCourses = true;
    
    @Column
    private boolean showClinical = true;
    
    @Column
    private boolean showExams = true;
    
    @Column
    private boolean showPersonal = true;
}