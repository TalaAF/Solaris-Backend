package com.example.lms.assessment.service;

import com.example.lms.assessment.dto.QuestionDTO;

import java.util.List;

public interface QuestionService {
    
    /**
     * Create a new question for a quiz
     *
     * @param questionDTO Question data to create
     * @return Created question
     */
    QuestionDTO.Response createQuestion(QuestionDTO.Request questionDTO);
    
    /**
     * Get a question by ID
     *
     * @param id Question ID
     * @return Question with the given ID
     */
    QuestionDTO.Response getQuestionById(Long id);
    
    /**
     * Get all questions for a quiz
     *
     * @param quizId Quiz ID
     * @return List of questions for the quiz
     */
    List<QuestionDTO.Response> getQuestionsByQuizId(Long quizId);
    
    /**
     * Get student view of all questions for a quiz
     * (without revealing correct answers)
     *
     * @param quizId Quiz ID
     * @return List of questions for students
     */
    List<QuestionDTO.StudentView> getQuestionsForStudent(Long quizId);
    
    /**
     * Update an existing question
     *
     * @param id Question ID
     * @param questionDTO Updated question data
     * @return Updated question
     */
    QuestionDTO.Response updateQuestion(Long id, QuestionDTO.Request questionDTO);
    
    /**
     * Delete a question
     *
     * @param id Question ID
     */
    void deleteQuestion(Long id);
    
  

    /**
     * Reorder questions in a quiz
     *
     * @param quizId Quiz ID
     * @param questionIds Ordered list of question IDs
     * @return Updated list of questions in new order
     */
    List<QuestionDTO.Response> reorderQuestions(Long quizId, List<Long> questionIds);
}