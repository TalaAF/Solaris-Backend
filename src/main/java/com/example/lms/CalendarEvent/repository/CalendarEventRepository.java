package com.example.lms.CalendarEvent.repository;


import com.example.lms.CalendarEvent.model.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByType(String type);
    List<CalendarEvent> findByDateContaining(String date);
    List<CalendarEvent> findByIsDeadlineTrue();
}