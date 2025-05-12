// src/main/java/com/example/lms/calender/mapper/CalendarMapper.java
package com.example.lms.calender.mapper;

import com.example.lms.calender.DTO.CalendarEventDTO;
import com.example.lms.calender.DTO.CalendarSettingsDTO;
import com.example.lms.calender.model.CalendarEvent;
import com.example.lms.calender.model.CalendarSettings;
import com.example.lms.calender.model.EventType;
import com.example.lms.calender.model.EventAudience;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CalendarMapper {

    private final UserRepository userRepository;

    @Autowired
    public CalendarMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CalendarEventDTO toDTO(CalendarEvent event) {
        return CalendarEventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .date(event.getDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .description(event.getDescription())
                .type(event.getType())
                .userId(event.getUser().getId())
                .userName(event.getUser().getFullName())
                .shared(event.isShared())
                .audience(event.getAudience())
                .externalEventId(event.getExternalEventId())
                .build();
    }

    public CalendarEvent toEntity(CalendarEventDTO dto, User user) {
        return CalendarEvent.builder()
                .title(dto.getTitle())
                .date(dto.getDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .location(dto.getLocation())
                .description(dto.getDescription())
                .type(dto.getType())
                .user(user)
                .shared(dto.isShared())
                .audience(dto.getAudience())
                .externalEventId(dto.getExternalEventId())
                .build();
    }
    
    public CalendarEvent updateEntity(CalendarEvent entity, CalendarEventDTO dto) {
        entity.setTitle(dto.getTitle());
        entity.setDate(dto.getDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setLocation(dto.getLocation());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setShared(dto.isShared());
        entity.setAudience(dto.getAudience());
        return entity;
    }

    public CalendarSettingsDTO toDTO(CalendarSettings settings) {
        return CalendarSettingsDTO.builder()
                .id(settings.getId())
                .userId(settings.getUser().getId())
                .externalCalendarType(settings.getExternalCalendarType())
                .syncEnabled(settings.isSyncEnabled())
                .showCourses(settings.isShowCourses())
                .showClinical(settings.isShowClinical())
                .showExams(settings.isShowExams())
                .showPersonal(settings.isShowPersonal())
                .build();
    }

    public CalendarSettings toEntity(CalendarSettingsDTO dto, User user) {
        return CalendarSettings.builder()
                .user(user)
                .externalCalendarType(dto.getExternalCalendarType())
                .syncEnabled(dto.isSyncEnabled())
                .showCourses(dto.isShowCourses())
                .showClinical(dto.isShowClinical())
                .showExams(dto.isShowExams())
                .showPersonal(dto.isShowPersonal())
                .build();
    }
    
    public CalendarSettings updateEntity(CalendarSettings entity, CalendarSettingsDTO dto) {
        entity.setExternalCalendarType(dto.getExternalCalendarType());
        entity.setSyncEnabled(dto.isSyncEnabled());
        entity.setShowCourses(dto.isShowCourses());
        entity.setShowClinical(dto.isShowClinical());
        entity.setShowExams(dto.isShowExams());
        entity.setShowPersonal(dto.isShowPersonal());
        return entity;
    }
}