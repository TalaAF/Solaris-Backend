package com.example.lms.CalendarEvent.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class CalendarEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String type; // e.g., Lectures, Labs, Exams & Assessments, Personal
    private String date;
    private boolean isDeadline;
}