package com.example.lms.assessment.model;

import com.example.lms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "answer_options")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerOption extends BaseEntity {
    
    @Column(nullable = false)
    private String text;
    
    @Column(nullable = false)
    private boolean isCorrect;
    
    @Column
    private String feedback; // Specific feedback for this option
    
    @Column
    private Integer orderIndex; // For option ordering
    
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}