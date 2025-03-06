package com.example.lms.progress.model;

import com.example.lms.course.model.Content;
import com.example.lms.user.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_progress")
public class ContentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    private Double progress; // e.g., percentage of video watched
    private LocalDateTime lastUpdated;

    public ContentProgress() {}

    public ContentProgress(User student, Content content, Double progress) {
        this.student = student;
        this.content = content;
        this.progress = progress;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
}
