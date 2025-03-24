package com.example.lms.assessment.service;

import com.example.lms.assessment.dto.QuizDTO;
import com.example.lms.assessment.model.Quiz;
import com.example.lms.common.Exception.ResourceNotFoundException;

import java.util.List;

public interface QuizService {
    
    /**
     * Create a new quiz
     *
     * @param quizDTO Quiz data to create
     * @return Created quiz
     */
    QuizDTO.Response createQuiz(QuizDTO.Request quizDTO);
    
    /**
     * Get a quiz by ID
     *
     * @param id Quiz ID
     * @return Quiz with the given ID
     * @throws ResourceNotFoundException if quiz not found
     */
    QuizDTO.Response getQuizById(Long id);
    
    /**
     * Get a quiz with detailed information including all questions
     *
     * @param id Quiz ID
     * @return Detailed quiz with questions
     * @throws ResourceNotFoundException if quiz not found
     */
    QuizDTO.DetailedResponse getQuizWithQuestions(Long id);
    
    /**
     * Get all quizzes for a course
     *
     * @param courseId Course ID
     * @return List of quizzes for the course
     */
    List<QuizDTO.Response> getQuizzesByCourse(Long courseId);
    
    /**
     * Get all published quizzes for a course
     *
     * @param courseId Course ID
     * @return List of published quizzes for the course
     */
    List<QuizDTO.Response> getPublishedQuizzesByCourse(Long courseId);
    
    /**
     * Get all currently available quizzes for a course
     *
     * @param courseId Course ID
     * @return List of available quizzes for the course
     */
    List<QuizDTO.Response> getAvailableQuizzesByCourse(Long courseId);
    
    /**
     * Get a student view of a quiz (with limited information)
     *
     * @param quizId Quiz ID
     * @param studentId Student ID
     * @return Student view of the quiz
     * @throws ResourceNotFoundException if quiz not found
     */
    QuizDTO.StudentView getQuizForStudent(Long quizId, Long studentId);
    
    /**
     * Update an existing quiz
     *
     * @param id Quiz ID
     * @param quizDTO Updated quiz data
     * @return Updated quiz
     * @throws ResourceNotFoundException if quiz not found
     */
    QuizDTO.Response updateQuiz(Long id, QuizDTO.Request quizDTO);
    
    /**
     * Delete a quiz
     *
     * @param id Quiz ID
     * @throws ResourceNotFoundException if quiz not found
     */
    void deleteQuiz(Long id);
    
    /**
     * Publish a quiz (make it available to students)
     *
     * @param id Quiz ID
     * @return Published quiz
     * @throws ResourceNotFoundException if quiz not found
     */
    QuizDTO.Response publishQuiz(Long id);
    
    /**
     * Unpublish a quiz (hide it from students)
     *
     * @param id Quiz ID
     * @return Unpublished quiz
     * @throws ResourceNotFoundException if quiz not found
     */
    QuizDTO.Response unpublishQuiz(Long id);
}