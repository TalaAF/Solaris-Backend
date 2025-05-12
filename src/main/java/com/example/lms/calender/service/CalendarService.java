// src/main/java/com/example/lms/calender/service/CalendarService.java
package com.example.lms.calender.service;

import com.example.lms.calender.DTO.CalendarEventDTO;
import com.example.lms.calender.DTO.CalendarSettingsDTO;
import com.example.lms.calender.mapper.CalendarMapper;
import com.example.lms.calender.model.CalendarEvent;
import com.example.lms.calender.model.CalendarSettings;
import com.example.lms.calender.model.EventAudience;
import com.example.lms.calender.model.EventType;
import com.example.lms.calender.repository.CalendarEventRepository;
import com.example.lms.calender.repository.CalendarSettingsRepository;
import com.example.lms.security.service.AuthenticationService;
import com.example.lms.user.model.User;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final CalendarEventRepository eventRepository;
    private final CalendarSettingsRepository settingsRepository;
    private final CalendarMapper mapper;
    private final AuthenticationService authService;
    private final ExternalCalendarService externalCalendarService;

    @Autowired
    public CalendarService(
            CalendarEventRepository eventRepository,
            CalendarSettingsRepository settingsRepository,
            CalendarMapper mapper,
            AuthenticationService authService,
            ExternalCalendarService externalCalendarService) {
        this.eventRepository = eventRepository;
        this.settingsRepository = settingsRepository;
        this.mapper = mapper;
        this.authService = authService;
        this.externalCalendarService = externalCalendarService;
    }

    /**
     * Get events for the current user with filters
     */
    @Transactional(readOnly = true)
    public List<CalendarEventDTO> getCurrentUserEvents(
            LocalDate startDate, 
            LocalDate endDate, 
            EventType type) {
        
        User currentUser = authService.getCurrentUser();
        
        // Default to 30 days from today if dates not provided
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(30);
        }
        
        // Determine audience based on user role
        EventAudience audience = null;
        if (authService.hasRole("STUDENT")) {
            audience = EventAudience.STUDENTS;
        } else if (authService.hasRole("INSTRUCTOR")) {
            audience = EventAudience.TEACHERS;
        }
        
        List<CalendarEvent> events;
        if (type != null) {
            events = eventRepository.findVisibleEventsByType(currentUser, audience, type, startDate, endDate);
        } else {
            events = eventRepository.findVisibleEvents(currentUser, audience, startDate, endDate);
        }
        
        return events.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific event by ID
     */
    @Transactional(readOnly = true)
    public CalendarEventDTO getEventById(Long id) {
        User currentUser = authService.getCurrentUser();
        
        CalendarEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        
        // Check if user has access to event
        if (!event.getUser().getId().equals(currentUser.getId()) && !event.isShared()) {
            throw new IllegalStateException("You don't have permission to view this event");
        }
        
        return mapper.toDTO(event);
    }

    /**
     * Create a new calendar event
     */
    @Transactional
    public CalendarEventDTO createEvent(CalendarEventDTO eventDTO) {
        User currentUser = authService.getCurrentUser();
        
        CalendarEvent event = mapper.toEntity(eventDTO, currentUser);
        CalendarEvent savedEvent = eventRepository.save(event);
        
        // Sync with external calendar if enabled
        if (event.isShared() || eventDTO.getExternalEventId() == null) {
            syncEventToExternalCalendar(savedEvent);
        }
        
        return mapper.toDTO(savedEvent);
    }

    /**
     * Update an existing calendar event
     */
    @Transactional
    public CalendarEventDTO updateEvent(Long eventId, CalendarEventDTO eventDTO) {
        User currentUser = authService.getCurrentUser();
        
        CalendarEvent existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        
        // Check if user is owner of the event
        if (!existingEvent.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You don't have permission to update this event");
        }
        
        // Update event fields
        CalendarEvent updatedEvent = mapper.updateEntity(existingEvent, eventDTO);
        updatedEvent = eventRepository.save(updatedEvent);
        
        // Update in external calendar if connected
        if (existingEvent.getExternalEventId() != null) {
            syncEventUpdateToExternalCalendar(updatedEvent);
        }
        
        return mapper.toDTO(updatedEvent);
    }

    /**
     * Delete a calendar event
     */
    @Transactional
    public void deleteEvent(Long eventId) {
        User currentUser = authService.getCurrentUser();
        
        CalendarEvent existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        
        // Check if user is owner of the event
        if (!existingEvent.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You don't have permission to delete this event");
        }
        
        // Delete from external calendar if connected
        if (existingEvent.getExternalEventId() != null) {
            deleteEventFromExternalCalendar(existingEvent);
        }
        
        eventRepository.delete(existingEvent);
    }

    /**
     * Get calendar settings for the current user
     */
    @Transactional(readOnly = true)
    public CalendarSettingsDTO getUserCalendarSettings() {
        User currentUser = authService.getCurrentUser();
        
        CalendarSettings settings = settingsRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    // Create default settings if not exist
                    CalendarSettings defaultSettings = new CalendarSettings();
                    defaultSettings.setUser(currentUser);
                    return settingsRepository.save(defaultSettings);
                });
        
        return mapper.toDTO(settings);
    }

    /**
     * Update calendar settings for the current user
     */
    @Transactional
    public CalendarSettingsDTO updateCalendarSettings(CalendarSettingsDTO settingsDTO) {
        User currentUser = authService.getCurrentUser();
        
        CalendarSettings existingSettings = settingsRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    // Create new settings if not exist
                    CalendarSettings newSettings = new CalendarSettings();
                    newSettings.setUser(currentUser);
                    return newSettings;
                });
        
        // Update settings
        CalendarSettings updatedSettings = mapper.updateEntity(existingSettings, settingsDTO);
        updatedSettings = settingsRepository.save(updatedSettings);
        
        return mapper.toDTO(updatedSettings);
    }

    /**
     * Connect to external calendar service
     */
    @Transactional
    public void connectExternalCalendar(String calendarType, String authCode) {
        User currentUser = authService.getCurrentUser();
        
        // Get or create settings
        CalendarSettings settings = settingsRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    CalendarSettings newSettings = new CalendarSettings();
                    newSettings.setUser(currentUser);
                    return newSettings;
                });
        
        // Connect to external calendar service
        String calendarId = externalCalendarService.connect(currentUser, calendarType, authCode);
        
        // Update settings
        settings.setExternalCalendarId(calendarId);
        settings.setExternalCalendarType(calendarType);
        settings.setSyncEnabled(true);
        settingsRepository.save(settings);
    }

    /**
     * Sync with external calendar
     */
    @Transactional
    public void syncWithExternalCalendar() {
        // Get all users with sync enabled
        List<CalendarSettings> syncEnabledSettings = settingsRepository.findBySyncEnabled();
        
        for (CalendarSettings settings : syncEnabledSettings) {
            // Fetch events from external calendar
            List<CalendarEvent> externalEvents = externalCalendarService.fetchEvents(
                    settings.getUser(), settings);
            
            // Save events to database
            for (CalendarEvent event : externalEvents) {
                if (event.getExternalEventId() != null) {
                    eventRepository.save(event);
                }
            }
        }
    }

    /**
     * Create event in external calendar
     */
    private void syncEventToExternalCalendar(CalendarEvent event) {
        User user = event.getUser();
        CalendarSettings settings = settingsRepository.findByUser(user).orElse(null);
        
        if (settings != null && settings.isSyncEnabled()) {
            String externalEventId = externalCalendarService.createEvent(event, settings);
            if (externalEventId != null) {
                event.setExternalEventId(externalEventId);
                eventRepository.save(event);
            }
        }
    }

    /**
     * Update event in external calendar
     */
    private void syncEventUpdateToExternalCalendar(CalendarEvent event) {
        User user = event.getUser();
        CalendarSettings settings = settingsRepository.findByUser(user).orElse(null);
        
        if (settings != null && settings.isSyncEnabled() && event.getExternalEventId() != null) {
            externalCalendarService.updateEvent(event, settings);
        }
    }

    /**
     * Delete event from external calendar
     */
    private void deleteEventFromExternalCalendar(CalendarEvent event) {
        User user = event.getUser();
        CalendarSettings settings = settingsRepository.findByUser(user).orElse(null);
        
        if (settings != null && settings.isSyncEnabled() && event.getExternalEventId() != null) {
            externalCalendarService.deleteEvent(event, settings);
        }
    }
}