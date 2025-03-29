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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;

@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
@Tag(name = "Quiz Attempt Management", description = "API endpoints for managing quiz attempts")
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
    @Operation(summary = "Start a new quiz attempt", description = "Create a new quiz attempt for a student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quiz attempt started successfully",
                    content = @Content(schema = @Schema(implementation = QuizAttemptDTO.InProgressAttempt.class))),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
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
    @Operation(summary = "Get in-progress quiz attempt", description = "Retrieve an in-progress quiz attempt by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "In-progress quiz attempt retrieved successfully",
                    content = @Content(schema = @Schema(implementation = QuizAttemptDTO.InProgressAttempt.class))),
            @ApiResponse(responseCode = "404", description = "Quiz attempt not found")
    })
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
    @Operation(summary = "Submit an answer", description = "Submit an answer to a question in a quiz attempt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Answer submitted successfully",
                    content = @Content(schema = @Schema(implementation = StudentAnswerDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
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
    @Operation(summary = "Submit quiz attempt", description = "Submit a quiz attempt for grading")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz attempt submitted successfully",
                    content = @Content(schema = @Schema(implementation = QuizAttemptDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
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
    @Operation(summary = "Get quiz attempt by ID", description = "Retrieve a quiz attempt by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz attempt found",
                    content = @Content(schema = @Schema(implementation = QuizAttemptDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Quiz attempt not found")
    })
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
    @Operation(summary = "Get detailed quiz attempt", description = "Retrieve a detailed quiz attempt with all answers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detailed quiz attempt found",
                    content = @Content(schema = @Schema(implementation = QuizAttemptDTO.DetailedResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quiz attempt not found")
    })
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
    @Operation(summary = "Get all attempts for a quiz", description = "Retrieve all attempts for a specific quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz attempts found",
                    content = @Content(schema = @Schema(implementation = QuizAttemptDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
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
    @Operation(summary = "Get all attempts by a student", description = "Retrieve all quiz attempts by a specific student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz attempts found",
                    content = @Content(schema = @Schema(implementation = QuizAttemptDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
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
    @Operation(summary = "Get all attempts for a quiz by a student", description = "Retrieve all quiz attempts for a specific quiz by a specific student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz attempts found",
                    content = @Content(schema = @Schema(implementation = QuizAttemptDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Quiz or student not found")
    })
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
    @Operation(summary = "Grade a manually graded answer", description = "Grade a manually graded answer for a quiz attempt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Answer graded successfully",
                    content = @Content(schema = @Schema(implementation = StudentAnswerDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    public ResponseEntity<StudentAnswerDTO.Response> gradeAnswer(
            @Valid @RequestBody StudentAnswerDTO.GradeRequest gradeRequest) {
        StudentAnswerDTO.Response answer = quizAttemptService.gradeAnswer(gradeRequest);
        return ResponseEntity.ok(answer);
    }
}