package com.example.lms.assessment.model;

import com.example.lms.common.BaseEntity;
import com.example.lms.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "quiz_attempts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime submittedAt;

    @Column
    private Double score; // Total score achieved

    @Column
    private Double percentageScore; // Score as a percentage

    @Column
    private boolean passed; // Whether the attempt met the passing score

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentAnswer> answers = new ArrayList<>();

    // Utility methods
    public void addAnswer(StudentAnswer answer) {
        answers.add(answer);
        answer.setAttempt(this);
    }

    public boolean isInProgress() {
        return status == AttemptStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == AttemptStatus.COMPLETED;
    }

    public boolean isTimedOut() {
        return status == AttemptStatus.TIMED_OUT;
    }

    public Double calculateTotalScore() {
        return answers.stream()
                .mapToDouble(StudentAnswer::getScore)
                .sum();
    }

    public Double calculatePercentageScore() {
        double totalPoints = quiz.getTotalPossibleScore();
        if (totalPoints == 0) return 0.0;
        return (calculateTotalScore() / totalPoints) * 100;
    }

    public void finalizeAttempt() {
        this.submittedAt = LocalDateTime.now();
        this.status = AttemptStatus.COMPLETED;
        this.score = calculateTotalScore();
        this.percentageScore = calculatePercentageScore();
        this.passed = this.percentageScore >= quiz.getPassingScore();
    }
}