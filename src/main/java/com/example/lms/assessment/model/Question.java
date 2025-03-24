package com.example.lms.assessment.model;

import com.example.lms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "questions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends BaseEntity {

    @Column(nullable = false)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column
    private Integer points; // Point value for this question

    @Column
    private Integer orderIndex; // For question ordering in quiz

    @Column(columnDefinition = "TEXT")
    private String feedback; // General feedback after answering

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerOption> options = new ArrayList<>();

    // Utility methods
    public void addOption(AnswerOption option) {
        options.add(option);
        option.setQuestion(this);
    }

    public void removeOption(AnswerOption option) {
        options.remove(option);
        option.setQuestion(null);
    }

    public List<AnswerOption> getCorrectOptions() {
        return options.stream()
                .filter(AnswerOption::isCorrect)
                .toList();
    }

    public boolean hasMultipleCorrectAnswers() {
        return getCorrectOptions().size() > 1;
    }
}