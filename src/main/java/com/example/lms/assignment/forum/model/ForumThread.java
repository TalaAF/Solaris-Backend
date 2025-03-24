package com.example.lms.assignment.forum.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "forum_threads")
public class ForumThread {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private LocalDateTime createdDate;
}