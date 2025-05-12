package com.example.lms.calender.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarSettingsDTO {
    private Long id;
    private Long userId;
    private String externalCalendarType; // Changed to String to match model
    private boolean syncEnabled;
    private boolean showCourses;
    private boolean showClinical;
    private boolean showExams;
    private boolean showPersonal;
}