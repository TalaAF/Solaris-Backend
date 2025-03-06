package com.example.lms.progress.model;

import com.example.lms.content.model.Content;
import com.example.lms.enrollment.model.Enrollment;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "content_progress")
public class ContentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment; // Tracks which student is enrolled in the course

    @ManyToOne
    @JoinColumn(name = "content_id", nullable = false)
    private Content content; // The specific content being tracked

    private Double progress; // e.g., percentage of video watched
    private LocalDateTime lastUpdated; // Timestamp of when progress was last updated

    public boolean isCompleted() {
        return this.progress != null && this.progress >= 100.0;
    }

    public void updateProgress(Double newProgress) {
        if (newProgress < 0 || newProgress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100.");
        }
        this.progress = newProgress;
        this.lastUpdated = LocalDateTime.now(); // Explicitly update the timestamp
    }

    // Getter for lastUpdated (if not using @Data from Lombok, you'd need this getter)
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}
