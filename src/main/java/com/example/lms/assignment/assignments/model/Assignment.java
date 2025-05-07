package com.example.lms.assignment.assignments.model;

import com.example.lms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "assignments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment extends BaseEntity {
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column
    private LocalDateTime dueDate;
    
    @Column(nullable = false)
    private Integer maxScore;
    
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean published;
    
    @Column(nullable = false)
    private Long courseId;
}