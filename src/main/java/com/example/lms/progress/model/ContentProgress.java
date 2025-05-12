package com.example.lms.progress.model;

import com.example.lms.content.model.Content;
import com.example.lms.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_progress")
@Getter
@Setter
public class ContentProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;
    
    // Add this field to satisfy the database constraint temporarily
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    private Double progress;

    private Boolean completed;

    @Column(name = "first_viewed")
    private LocalDateTime firstViewed;

    @Column(name = "last_viewed")
    private LocalDateTime lastViewed;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
