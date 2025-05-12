// src/main/java/com/example/lms/calender/controller/CalendarController.java
package com.example.lms.calender.controller;

import com.example.lms.calender.DTO.CalendarEventDTO;
import com.example.lms.calender.DTO.CalendarSettingsDTO;
import com.example.lms.calender.assembler.CalendarEventAssembler;
import com.example.lms.calender.model.EventType;
import com.example.lms.calender.service.CalendarService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;
    private final CalendarEventAssembler eventAssembler;

    @Autowired
    public CalendarController(CalendarService calendarService, CalendarEventAssembler eventAssembler) {
        this.calendarService = calendarService;
        this.eventAssembler = eventAssembler;
    }

    @GetMapping("/events")
    public ResponseEntity<List<CalendarEventDTO>> getEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) EventType type) {
        return ResponseEntity.ok(calendarService.getCurrentUserEvents(startDate, endDate, type));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EntityModel<CalendarEventDTO>> getEventById(@PathVariable Long id) {
        CalendarEventDTO event = calendarService.getEventById(id);
        return ResponseEntity.ok(eventAssembler.toModel(event));
    }

    @GetMapping("/events/date/{date}")
    public ResponseEntity<List<EntityModel<CalendarEventDTO>>> getEventsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // Call getEvents with same date for start and end to get events for specific date
        List<CalendarEventDTO> events = calendarService.getCurrentUserEvents(date, date, null);
        List<EntityModel<CalendarEventDTO>> eventModels = events.stream()
            .map(eventAssembler::toModel)
            .collect(Collectors.toList());
        return ResponseEntity.ok(eventModels);
    }

    @PostMapping("/events")
    public ResponseEntity<CalendarEventDTO> createEvent(@RequestBody CalendarEventDTO eventDTO) {
        return ResponseEntity.ok(calendarService.createEvent(eventDTO));
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<CalendarEventDTO> updateEvent(
            @PathVariable Long eventId,
            @RequestBody CalendarEventDTO eventDTO) {
        return ResponseEntity.ok(calendarService.updateEvent(eventId, eventDTO));
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        calendarService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/settings")
    public ResponseEntity<CalendarSettingsDTO> getCalendarSettings() {
        return ResponseEntity.ok(calendarService.getUserCalendarSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<CalendarSettingsDTO> updateCalendarSettings(
            @RequestBody CalendarSettingsDTO settingsDTO) {
        return ResponseEntity.ok(calendarService.updateCalendarSettings(settingsDTO));
    }

    @PostMapping("/connect")
    public ResponseEntity<Void> connectExternalCalendar(
            @RequestParam String calendarType,
            @RequestParam String authCode) {
        calendarService.connectExternalCalendar(calendarType, authCode);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncCalendar() {
        calendarService.syncWithExternalCalendar();
        return ResponseEntity.ok().build();
    }
}