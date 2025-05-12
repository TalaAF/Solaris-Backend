package com.example.lms.CalendarEvent.service;



import com.example.lms.CalendarEvent.DTO.CalendarEventDTO;
import com.example.lms.CalendarEvent.mapper.CalendarEventMapper;
import com.example.lms.CalendarEvent.model.CalendarEvent;
import com.example.lms.CalendarEvent.repository.CalendarEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarService {
    @Autowired
    private CalendarEventRepository repository;
    @Autowired
    private CalendarEventMapper mapper;

    public List<CalendarEventDTO> getMonthEvents(String month) {
        return repository.findByDateContaining(month)
                .stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public List<CalendarEventDTO> getWeekEvents(String startDate, String endDate) {
        return repository.findByDateContaining(startDate)
                .stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public List<CalendarEventDTO> getDayEvents(String date) {
        return repository.findByDateContaining(date)
                .stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public List<CalendarEventDTO> getEventsByType(String type) {
        return repository.findByType(type)
                .stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public List<CalendarEventDTO> getUpcomingDeadlines() {
        return repository.findByIsDeadlineTrue()
                .stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public CalendarEventDTO createEvent(CalendarEventDTO dto) {
        CalendarEvent event = mapper.toEntity(dto);
        return mapper.toDTO(repository.save(event));
    }

    public void deleteEvent(Long id) {
        repository.deleteById(id);
    }
}