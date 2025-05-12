package com.example.lms.CalendarEvent.mapper;


import com.example.lms.CalendarEvent.DTO.CalendarEventDTO;
import com.example.lms.CalendarEvent.model.CalendarEvent;
import org.springframework.stereotype.Component;

@Component
public class CalendarEventMapper {
    public CalendarEventDTO toDTO(CalendarEvent event) {
        CalendarEventDTO dto = new CalendarEventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setType(event.getType());
        dto.setDate(event.getDate());
        dto.setDeadline(event.isDeadline());
        return dto;
    }

    public CalendarEvent toEntity(CalendarEventDTO dto) {
        CalendarEvent event = new CalendarEvent();
        event.setId(dto.getId());
        event.setTitle(dto.getTitle());
        event.setType(dto.getType());
        event.setDate(dto.getDate());
        event.setDeadline(dto.isDeadline());
        return event;
    }
}