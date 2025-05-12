// src/main/java/com/example/lms/calender/DTO/CalendarEventDTO.java
package com.example.lms.calender.DTO;

import com.example.lms.calender.model.EventAudience;
import com.example.lms.calender.model.EventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDTO {
    private Long id;
    private String title;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String description;
    private EventType type;
    private EventAudience audience;
    private Long userId;
    private String userName;
    private boolean shared;
    private String externalEventId;
}