package com.example.lms.course.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "completion_requirement")
public class CompletionRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course; // The course to which this requirement belongs

    private Double requiredProgress; // e.g., required percentage of content watched to complete the course
    private Boolean quizPassedRequired; // Whether passing a quiz is required

  
}
