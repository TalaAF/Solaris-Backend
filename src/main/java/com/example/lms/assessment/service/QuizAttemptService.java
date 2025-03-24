package com.example.lms.assessment.service;

import com.example.lms.assessment.dto.QuizAttemptDTO;
import com.example.lms.assessment.dto.StudentAnswerDTO;

import java.util.List;

public interface QuizAttemptService {
    
    /**
     * Start a new quiz attempt
     *
     * @param startRequest Request with quiz and student IDs
     * @return Created quiz attempt with questions
     */
    QuizAttemptDTO.InProgressAttempt startQuizAttempt(QuizAttemptDTO.StartRequest startRequest);
    
    /**
     * Get an in-progress quiz attempt
     *
     * @param attemptId Attempt ID
     * @return Quiz attempt with questions
     */
    QuizAttemptDTO.InProgressAttempt getInProgressAttempt(Long attemptId);
    
    /**
     * Submit an answer to a question in a quiz attempt
     *
     * @param answerRequest Answer data
     * @return Updated answer
     */
    StudentAnswerDTO.Response submitAnswer(StudentAnswerDTO.SubmitRequest answerRequest);
    
    /**
     * Submit a quiz attempt for grading
     *
     * @param submitRequest Request with attempt ID
     * @return Completed quiz attempt
     */
    QuizAttemptDTO.Response submitQuizAttempt(QuizAttemptDTO.SubmitRequest submitRequest);
    
    /**
     * Get a quiz attempt by ID
     *
     * @param attemptId Attempt ID
     * @return Quiz attempt
     */
    QuizAttemptDTO.Response getQuizAttempt(Long attemptId);
    
    /**
     * Get a detailed quiz attempt with all answers
     *
     * @param attemptId Attempt ID
     * @return Detailed quiz attempt
     */
    QuizAttemptDTO.DetailedResponse getDetailedQuizAttempt(Long attemptId);
    
    /**
     * Get all attempts for a quiz
     *
     * @param quizId Quiz ID
     * @return List of quiz attempts
     */
    List<QuizAttemptDTO.Response> getAttemptsByQuiz(Long quizId);
    
    /**
     * Get all attempts by a student
     *
     * @param studentId Student ID
     * @return List of quiz attempts
     */
    List<QuizAttemptDTO.Response> getAttemptsByStudent(Long studentId);
    
    /**
     * Get all attempts for a quiz by a student
     *
     * @param quizId Quiz ID
     * @param studentId Student ID
     * @return List of quiz attempts
     */
    List<QuizAttemptDTO.Response> getAttemptsByQuizAndStudent(Long quizId, Long studentId);
    
    /**
     * Grade a manually graded answer
     *
     * @param gradeRequest Grade data
     * @return Updated answer
     */
    StudentAnswerDTO.Response gradeAnswer(StudentAnswerDTO.GradeRequest gradeRequest);
}