package com.example.lms.assessment.model;

public enum QuestionType {
    MULTIPLE_CHOICE,     // Select one from multiple options
    MULTIPLE_ANSWER,     // Select multiple from multiple options
    TRUE_FALSE,          // True or False question
    SHORT_ANSWER,        // Short text response
    ESSAY                // Longer text response (manually graded)
}