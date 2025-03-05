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
@Table(name = "enrollments")
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

    // Add progress tracking field
    @Column(nullable = false)
    private Double progress = 0.0; // Default to 0%

}
