// src/main/java/com/example/lms/calendar/repository/CalendarSettingsRepository.java
package com.example.lms.calender.repository;

import com.example.lms.calender.model.CalendarSettings;
import com.example.lms.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarSettingsRepository extends JpaRepository<CalendarSettings, Long> {
    
    Optional<CalendarSettings> findByUser(User user);
    
    @Query("SELECT cs FROM CalendarSettings cs WHERE cs.syncEnabled = true")
    List<CalendarSettings> findBySyncEnabled();
}