package com.example.lms.CalendarEvent.controller;



import com.example.lms.CalendarEvent.DTO.CalendarEventDTO;
import com.example.lms.CalendarEvent.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
    @Autowired
    private CalendarService service;

    @GetMapping("/month/{month}")
    public List<CalendarEventDTO> getMonthEvents(@PathVariable String month) {
        return service.getMonthEvents(month);
    }

    @GetMapping("/week/{startDate}/{endDate}")
    public List<CalendarEventDTO> getWeekEvents(@PathVariable String startDate, @PathVariable String endDate) {
        return service.getWeekEvents(startDate, endDate);
    }

    @GetMapping("/day/{date}")
    public List<CalendarEventDTO> getDayEvents(@PathVariable String date) {
        return service.getDayEvents(date);
    }

    @GetMapping("/events/{type}")
    public List<CalendarEventDTO> getEventsByType(@PathVariable String type) {
        return service.getEventsByType(type);
    }

    @GetMapping("/deadlines")
    public List<CalendarEventDTO> getUpcomingDeadlines() {
        return service.getUpcomingDeadlines();
    }

    @PostMapping("/event")
    public CalendarEventDTO createEvent(@RequestBody CalendarEventDTO dto) {
        return service.createEvent(dto);
    }

    @PutMapping("/event/{id}")
    public CalendarEventDTO updateEvent(@PathVariable Long id, @RequestBody CalendarEventDTO dto) {
        dto.setId(id);
        return service.createEvent(dto); // Reuse createEvent for simplicity; in production, add update logic in service
    }

    @DeleteMapping("/event/{id}")
    public void deleteEvent(@PathVariable Long id) {
        service.deleteEvent(id);
    }
}
