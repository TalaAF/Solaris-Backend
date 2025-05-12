package com.example.lms.CalendarEvent.DTO;



import lombok.Data;

@Data
public class CalendarEventDTO {
    private Long id;
    private String title;
    private String type;
    private String date;
    private boolean isDeadline;
}
