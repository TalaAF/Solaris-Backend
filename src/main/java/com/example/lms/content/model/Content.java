package com.example.lms.content.model;

import com.example.lms.course.model.Course;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ContentType type; // VIDEO, ARTICLE, QUIZ, etc.

    private int duration; // Duration in minutes (for videos/articles)

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
