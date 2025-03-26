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
@Table(name = "student_answers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAnswer extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToMany
    @JoinTable(
        name = "student_answer_options",
        joinColumns = @JoinColumn(name = "student_answer_id"),
        inverseJoinColumns = @JoinColumn(name = "answer_option_id")
    )
    private List<AnswerOption> selectedOptions = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String textAnswer; // For short answer/essay questions

    @Column
    private Double score; // Points awarded for this answer

    @Column
    private boolean isCorrect; // Whether the answer is correct

    // For grading essay or short answer questions
    @Column
    private boolean manuallyGraded;

    @Column
    private String instructorFeedback;

    // Utility methods
    public void selectOption(AnswerOption option) {
        if (!selectedOptions.contains(option)) {
            selectedOptions.add(option);
        }
    }

    public void unselectOption(AnswerOption option) {
        selectedOptions.remove(option);
    }

    public void autoGrade() {
        QuestionType type = question.getType();
        int questionPoints = question.getPoints();

        switch (type) {
            case MULTIPLE_CHOICE, TRUE_FALSE -> {
                // For single-select questions, only correct if the exact correct option is selected
                if (selectedOptions.size() == 1 && selectedOptions.get(0).isCorrect()) {
                    this.isCorrect = true;
                    this.score = (double) questionPoints;
                } else {
                    this.isCorrect = false;
                    this.score = 0.0;
                }
            }
            case MULTIPLE_ANSWER -> {
                // For multiple-select questions, partial credit based on correct selections
                int totalOptions = question.getOptions().size();
                int correctOptions = question.getCorrectOptions().size();
                
                // Count correct selections and incorrect selections
                int correctSelections = 0;
                int incorrectSelections = 0;
                
                for (AnswerOption option : selectedOptions) {
                    if (option.isCorrect()) {
                        correctSelections++;
                    } else {
                        incorrectSelections++;
                    }
                }
                
                // Calculate partial credit - get points for correct selections, lose points for incorrect ones
                double correctRatio = (double) correctSelections / correctOptions;
                double incorrectPenalty = (double) incorrectSelections / (totalOptions - correctOptions);
                
                double partialScore = correctRatio - incorrectPenalty;
                if (partialScore < 0) partialScore = 0;
                
                this.score = partialScore * questionPoints;
                this.isCorrect = correctSelections == correctOptions && incorrectSelections == 0;
            }
            case SHORT_ANSWER -> {
                // Short answer can be auto-graded with exact match
                if (textAnswer != null && !textAnswer.isEmpty()) {
                    List<AnswerOption> correctOptions = question.getCorrectOptions();
                    for (AnswerOption option : correctOptions) {
                        if (textAnswer.trim().equalsIgnoreCase(option.getText().trim())) {
                            this.isCorrect = true;
                            this.score = (double) questionPoints;
                            return;
                        }
                    }
                }
                this.isCorrect = false;
                this.score = 0.0;
            }
            case ESSAY -> {
                // Essay questions require manual grading
                this.manuallyGraded = true;
                this.score = 0.0; // Will be updated after manual grading
                this.isCorrect = false; // Will be updated after manual grading
            }
        }
    }
}