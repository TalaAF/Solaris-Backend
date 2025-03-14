package com.example.lms.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class SystemAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long totalUsers;
    private Long totalCourses;
    private Long totalLogins;
    private Long activeUsers;
    private LocalDateTime lastUpdated;
}
