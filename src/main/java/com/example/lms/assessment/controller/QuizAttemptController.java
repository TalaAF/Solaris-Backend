package com.example.lms.assessment.controller;

import com.example.lms.assessment.dto.QuizAttemptDTO;
import com.example.lms.assessment.dto.StudentAnswerDTO;
import com.example.lms.assessment.service.QuizAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    /**
     * Start a new quiz attempt
     *
     * @param startRequest Request with quiz and student IDs
     * @return Created quiz attempt with questions
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizAttemptDTO.InProgressAttempt> startQuizAttempt(
            @Valid @RequestBody QuizAttemptDTO.StartRequest startRequest) {
        QuizAttemptDTO.InProgressAttempt attempt = quizAttemptService.startQuizAttempt(startRequest);
        return new ResponseEntity<>(attempt, HttpStatus.CREATED);
    }

    /**
     * Get an in-progress quiz attempt
     *
     * @param attemptId Attempt ID
     * @return Quiz attempt with questions
     */
    @GetMapping("/{attemptId}/in-progress")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizAttemptDTO.InProgressAttempt> getInProgressAttempt(@PathVariable Long attemptId) {
        QuizAttemptDTO.InProgressAttempt attempt = quizAttemptService.getInProgressAttempt(attemptId);
        return ResponseEntity.ok(attempt);
    }

    /**
     * Submit an answer to a question in a quiz attempt
     *
     * @param answerRequest Answer data
     * @return Updated answer
     */
    @PostMapping("/submit-answer")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<StudentAnswerDTO.Response> submitAnswer(
            @Valid @RequestBody StudentAnswerDTO.SubmitRequest answerRequest) {
        StudentAnswerDTO.Response answer = quizAttemptService.submitAnswer(answerRequest);
        return ResponseEntity.ok(answer);
    }

    /**
     * Submit a quiz attempt for grading
     *
     * @param submitRequest Request with attempt ID
     * @return Completed quiz attempt
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizAttemptDTO.Response> submitQuizAttempt(
            @Valid @RequestBody QuizAttemptDTO.SubmitRequest submitRequest) {
        QuizAttemptDTO.Response attempt = quizAttemptService.submitQuizAttempt(submitRequest);
        return ResponseEntity.ok(attempt);
    }

    /**
     * Get a quiz attempt by ID
     *
     * @param attemptId Attempt ID
     * @return Quiz attempt
     */
    @GetMapping("/{attemptId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizAttemptDTO.Response> getQuizAttempt(@PathVariable Long attemptId) {
        QuizAttemptDTO.Response attempt = quizAttemptService.getQuizAttempt(attemptId);
        return ResponseEntity.ok(attempt);
    }

    /**
     * Get a detailed quiz attempt with all answers
     *
     * @param attemptId Attempt ID
     * @return Detailed quiz attempt
     */
    @GetMapping("/{attemptId}/detailed")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizAttemptDTO.DetailedResponse> getDetailedQuizAttempt(@PathVariable Long attemptId) {
        QuizAttemptDTO.DetailedResponse attempt = quizAttemptService.getDetailedQuizAttempt(attemptId);
        return ResponseEntity.ok(attempt);
    }

    /**
     * Get all attempts for a quiz
     *
     * @param quizId Quiz ID
     * @return List of quiz attempts
     */
    @GetMapping("/quiz/{quizId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<QuizAttemptDTO.Response>> getAttemptsByQuiz(@PathVariable Long quizId) {
        List<QuizAttemptDTO.Response> attempts = quizAttemptService.getAttemptsByQuiz(quizId);
        return ResponseEntity.ok(attempts);
    }

    /**
     * Get all attempts by a student
     *
     * @param studentId Student ID
     * @return List of quiz attempts
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<QuizAttemptDTO.Response>> getAttemptsByStudent(@PathVariable Long studentId) {
        List<QuizAttemptDTO.Response> attempts = quizAttemptService.getAttemptsByStudent(studentId);
        return ResponseEntity.ok(attempts);
    }

    /**
     * Get all attempts for a quiz by a student
     *
     * @param quizId Quiz ID
     * @param studentId Student ID
     * @return List of quiz attempts
     */
    @GetMapping("/quiz/{quizId}/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<QuizAttemptDTO.Response>> getAttemptsByQuizAndStudent(
            @PathVariable Long quizId,
            @PathVariable Long studentId) {
        List<QuizAttemptDTO.Response> attempts = quizAttemptService.getAttemptsByQuizAndStudent(quizId, studentId);
        return ResponseEntity.ok(attempts);
    }
    
    /**
     * Grade a manually graded answer
     *
     * @param gradeRequest Grade data
     * @return Updated answer
     */
    @PostMapping("/grade-answer")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<StudentAnswerDTO.Response> gradeAnswer(
            @Valid @RequestBody StudentAnswerDTO.GradeRequest gradeRequest) {
        StudentAnswerDTO.Response answer = quizAttemptService.gradeAnswer(gradeRequest);
        return ResponseEntity.ok(answer);
    }
}