// src/main/java/com/example/lms/calender/repository/CalendarEventRepository.java
package com.example.lms.calender.repository;

import com.example.lms.calender.model.CalendarEvent;
import com.example.lms.calender.model.EventAudience;
import com.example.lms.calender.model.EventType;
import com.example.lms.user.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    
    List<CalendarEvent> findByUser(User user);
    
    List<CalendarEvent> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT e FROM CalendarEvent e WHERE e.user = :user OR e.shared = true")
    List<CalendarEvent> findVisibleEvents(@Param("user") User user);
    
    @Query("SELECT e FROM CalendarEvent e WHERE (e.user = :user OR e.shared = true) AND e.date BETWEEN :startDate AND :endDate")
    List<CalendarEvent> findVisibleEventsBetweenDates(
            @Param("user") User user, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e FROM CalendarEvent e WHERE " +
           "(e.user = :user OR " +
           "(e.shared = true AND " +
           "(:audience IS NULL OR e.audience = :audience OR e.audience = 'ALL'))) " +
           "AND e.type = :type " +
           "AND e.date BETWEEN :startDate AND :endDate")
    List<CalendarEvent> findVisibleEventsByType(
            @Param("user") User user, 
            @Param("audience") EventAudience audience,
            @Param("type") EventType type,
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e FROM CalendarEvent e WHERE " +
           "(e.user = :user OR " +
           "(e.shared = true AND " +
           "(:audience IS NULL OR e.audience = :audience OR e.audience = 'ALL'))) " +
           "AND e.date BETWEEN :startDate AND :endDate")
    List<CalendarEvent> findVisibleEvents(
            @Param("user") User user, 
            @Param("audience") EventAudience audience,
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    List<CalendarEvent> findByType(EventType type);
    
    List<CalendarEvent> findByExternalEventIdIsNotNull();
}