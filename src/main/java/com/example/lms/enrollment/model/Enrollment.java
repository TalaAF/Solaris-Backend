package com.example.lms.enrollment.model;

import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "course_id"})
})
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    private LocalDateTime enrollmentDate;
    
    private LocalDateTime completionDate;

    // Progress tracking field
    @Column(nullable = false)
    private Double progress = 0.0; // Default to 0%
    
    // Last accessed timestamp
    private LocalDateTime lastAccessedDate;
    
    // Method to check if enrollment is completed
    public boolean isCompleted() {
        return status == EnrollmentStatus.COMPLETED;
    }
    
    // Method to update progress
    public void updateProgress(Double newProgress) {
        if (newProgress < 0 || newProgress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100.");
        }
        this.progress = newProgress;
        this.lastAccessedDate = LocalDateTime.now();
        
        // Auto-complete if progress reaches 100%
        if (newProgress >= 100 && status != EnrollmentStatus.COMPLETED) {
            this.status = EnrollmentStatus.COMPLETED;
            this.completionDate = LocalDateTime.now();
        }
    }
}