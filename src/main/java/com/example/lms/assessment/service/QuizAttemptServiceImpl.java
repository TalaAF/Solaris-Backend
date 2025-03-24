package com.example.lms.assessment.service;

import com.example.lms.assessment.dto.AnswerOptionDTO;
import com.example.lms.assessment.dto.QuestionDTO;
import com.example.lms.assessment.dto.QuizAttemptDTO;
import com.example.lms.assessment.dto.StudentAnswerDTO;
import com.example.lms.assessment.model.*;
import com.example.lms.assessment.repository.AnswerOptionRepository;
import com.example.lms.assessment.repository.QuestionRepository;
import com.example.lms.assessment.repository.QuizAttemptRepository;
import com.example.lms.assessment.repository.QuizRepository;
import com.example.lms.assessment.repository.StudentAnswerRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionRepository questionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final AnswerOptionRepository answerOptionRepository;

    @Override
    @Transactional
    public QuizAttemptDTO.InProgressAttempt startQuizAttempt(QuizAttemptDTO.StartRequest startRequest) {
        Long quizId = startRequest.getQuizId();
        Long studentId = startRequest.getStudentId();
        
        // Fetch the quiz
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        
        // Validate quiz is published and available
        if (!quiz.isPublished()) {
            throw new IllegalStateException("Cannot start attempt for unpublished quiz");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if ((quiz.getStartDate() != null && now.isBefore(quiz.getStartDate())) ||
            (quiz.getEndDate() != null && now.isAfter(quiz.getEndDate()))) {
            throw new IllegalStateException("Quiz is not available at this time");
        }
        
        // Fetch the student
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        // Check if student already has an in-progress attempt
        Optional<QuizAttempt> existingAttempt = quizAttemptRepository
                .findByQuizIdAndStudentIdAndStatus(quizId, studentId, AttemptStatus.IN_PROGRESS);
        
        if (existingAttempt.isPresent()) {
            // Return existing attempt
            return mapToInProgressAttemptDTO(existingAttempt.get());
        }
        
        // Create new attempt
        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .startedAt(now)
                .status(AttemptStatus.IN_PROGRESS)
                .build();
        
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        
        // Return the attempt with questions
        return mapToInProgressAttemptDTO(savedAttempt);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizAttemptDTO.InProgressAttempt getInProgressAttempt(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found with id: " + attemptId));
        
        // Verify attempt is in progress
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Quiz attempt is not in progress");
        }
        
        return mapToInProgressAttemptDTO(attempt);
    }

    @Override
    @Transactional
    public StudentAnswerDTO.Response submitAnswer(StudentAnswerDTO.SubmitRequest answerRequest) {
        Long attemptId = answerRequest.getAttemptId();
        Long questionId = answerRequest.getQuestionId();
        
        // Fetch the attempt
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found with id: " + attemptId));
        
        // Verify attempt is in progress
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot submit answer for a completed quiz attempt");
        }
        
        // Fetch the question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
        
        // Check if this question belongs to the quiz in the attempt
        if (!question.getQuiz().getId().equals(attempt.getQuiz().getId())) {
            throw new IllegalArgumentException("Question does not belong to the quiz in this attempt");
        }
        
        // Check if there's an existing answer for this question in this attempt
        Optional<StudentAnswer> existingAnswerOpt = studentAnswerRepository
                .findByAttemptIdAndQuestionId(attemptId, questionId);
        
        StudentAnswer studentAnswer;
        if (existingAnswerOpt.isPresent()) {
            // Update existing answer
            studentAnswer = existingAnswerOpt.get();
            studentAnswer.getSelectedOptions().clear(); // Clear previous selections
        } else {
            // Create new answer
            studentAnswer = StudentAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedOptions(new ArrayList<>())
                    .build();
        }
        
        // Set text answer for essay/short answer questions
        if (question.getType() == QuestionType.ESSAY || question.getType() == QuestionType.SHORT_ANSWER) {
            studentAnswer.setTextAnswer(answerRequest.getTextAnswer());
        }
        
        // Add selected options for multiple choice/multiple answer questions
        if ((question.getType() == QuestionType.MULTIPLE_CHOICE || 
             question.getType() == QuestionType.MULTIPLE_ANSWER || 
             question.getType() == QuestionType.TRUE_FALSE) && 
            answerRequest.getSelectedOptionIds() != null && !answerRequest.getSelectedOptionIds().isEmpty()) {
            
            for (Long optionId : answerRequest.getSelectedOptionIds()) {
                AnswerOption option = answerOptionRepository.findById(optionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Answer option not found with id: " + optionId));
                
                // Verify option belongs to the question
                if (!option.getQuestion().getId().equals(questionId)) {
                    throw new IllegalArgumentException("Option does not belong to the question");
                }
                
                studentAnswer.getSelectedOptions().add(option);
            }
        }
        
        // Auto-grade the answer
        studentAnswer.autoGrade();
        
        // Save the answer
        StudentAnswer savedAnswer = studentAnswerRepository.save(studentAnswer);
        
        return mapToStudentAnswerResponseDTO(savedAnswer);
    }

    @Override
    @Transactional
    public QuizAttemptDTO.Response submitQuizAttempt(QuizAttemptDTO.SubmitRequest submitRequest) {
        Long attemptId = submitRequest.getAttemptId();
        
        // Fetch the attempt
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found with id: " + attemptId));
        
        // Verify attempt is in progress
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Quiz attempt is already submitted");
        }
        
        // Calculate scores and finalize the attempt
        attempt.finalizeAttempt();
        
        // Save the updated attempt
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        
        return mapToResponseDTO(savedAttempt);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizAttemptDTO.Response getQuizAttempt(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found with id: " + attemptId));
        
        return mapToResponseDTO(attempt);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizAttemptDTO.DetailedResponse getDetailedQuizAttempt(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found with id: " + attemptId));
        
        // Get all answers for this attempt
        List<StudentAnswer> answers = studentAnswerRepository.findByAttemptId(attemptId);
        
        // Map to response
        return mapToDetailedResponseDTO(attempt, answers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttemptDTO.Response> getAttemptsByQuiz(Long quizId) {
        // Verify quiz exists
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }
        
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        
        return attempts.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttemptDTO.Response> getAttemptsByStudent(Long studentId) {
        // Verify student exists
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(studentId);
        
        return attempts.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttemptDTO.Response> getAttemptsByQuizAndStudent(Long quizId, Long studentId) {
        // Verify quiz exists
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }
        
        // Verify student exists
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizIdAndStudentId(quizId, studentId);
        
        return attempts.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StudentAnswerDTO.Response gradeAnswer(StudentAnswerDTO.GradeRequest gradeRequest) {
        Long studentAnswerId = gradeRequest.getStudentAnswerId();
        
        // Fetch the student answer
        StudentAnswer answer = studentAnswerRepository.findById(studentAnswerId)
                .orElseThrow(() -> new ResourceNotFoundException("Student answer not found with id: " + studentAnswerId));
        
        // Verify answer requires manual grading
        if (!answer.isManuallyGraded()) {
            throw new IllegalStateException("This answer does not require manual grading");
        }
        
        // Update with instructor's grade
        answer.setScore(gradeRequest.getScore());
        answer.setInstructorFeedback(gradeRequest.getInstructorFeedback());
        
        // Determine if the answer is correct (based on threshold)
        double maxPoints = answer.getQuestion().getPoints();
        answer.setCorrect(answer.getScore() >= maxPoints * 0.5); // Correct if >= 50% of max points
        
        // Save the updated answer
        StudentAnswer savedAnswer = studentAnswerRepository.save(answer);
        
        // Recalculate overall attempt score
        QuizAttempt attempt = answer.getAttempt();
        attempt.setScore(attempt.calculateTotalScore());
        attempt.setPercentageScore(attempt.calculatePercentageScore());
        attempt.setPassed(attempt.getPercentageScore() >= attempt.getQuiz().getPassingScore());
        quizAttemptRepository.save(attempt);
        
        return mapToStudentAnswerResponseDTO(savedAnswer);
    }
    
    // Helper method to map QuizAttempt to InProgressAttemptDTO
    private QuizAttemptDTO.InProgressAttempt mapToInProgressAttemptDTO(QuizAttempt attempt) {
        Quiz quiz = attempt.getQuiz();
        
        // Get questions for the quiz
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quiz.getId());
        
        // Randomize questions if quiz requires it
        if (quiz.isRandomizeQuestions()) {
            Collections.shuffle(questions);
        }
        
        // Get student's existing answers
        List<StudentAnswer> existingAnswers = studentAnswerRepository.findByAttemptId(attempt.getId());
        
        // Map questions to DTOs with student's selections
        List<QuestionDTO.AttemptQuestion> questionDTOs = questions.stream()
                .map(question -> {
                    // Find student's answer for this question, if any
                    Optional<StudentAnswer> studentAnswer = existingAnswers.stream()
                            .filter(answer -> answer.getQuestion().getId().equals(question.getId()))
                            .findFirst();
                    
                    // Get all options for the question
                    List<AnswerOption> options = answerOptionRepository
                            .findByQuestionIdOrderByOrderIndexAsc(question.getId());
                    
                    // Map options to DTOs, marking selected ones
                    List<AnswerOptionDTO.AttemptOption> optionDTOs = options.stream()
                            .map(option -> {
                                boolean selected = studentAnswer.isPresent() && 
                                                 studentAnswer.get().getSelectedOptions().contains(option);
                                
                                return AnswerOptionDTO.AttemptOption.builder()
                                        .id(option.getId())
                                        .text(option.getText())
                                        .orderIndex(option.getOrderIndex())
                                        .selected(selected)
                                        .build();
                            })
                            .collect(Collectors.toList());
                    
                    // Get any text answer
                    String textAnswer = studentAnswer.isPresent() ? 
                                      studentAnswer.get().getTextAnswer() : null;
                    
                    return QuestionDTO.AttemptQuestion.builder()
                            .id(question.getId())
                            .text(question.getText())
                            .type(question.getType())
                            .points(question.getPoints())
                            .orderIndex(question.getOrderIndex())
                            .options(optionDTOs)
                            .textAnswer(textAnswer)
                            .build();
                })
                .collect(Collectors.toList());
        
        return QuizAttemptDTO.InProgressAttempt.builder()
                .id(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .startedAt(attempt.getStartedAt())
                .timeLimit(quiz.getTimeLimit())
                .status(attempt.getStatus())
                .questions(questionDTOs)
                .build();
    }
    
    // Helper method to map QuizAttempt to ResponseDTO
    private QuizAttemptDTO.Response mapToResponseDTO(QuizAttempt attempt) {
        int totalQuestions = questionRepository.findByQuizId(attempt.getQuiz().getId()).size();
        int answeredQuestions = studentAnswerRepository.findByAttemptId(attempt.getId()).size();
        
        return QuizAttemptDTO.Response.builder()
                .id(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .studentId(attempt.getStudent().getId())
                .studentName(attempt.getStudent().getFullName())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .score(attempt.getScore())
                .percentageScore(attempt.getPercentageScore())
                .passed(attempt.isPassed())
                .status(attempt.getStatus())
                .totalQuestions(totalQuestions)
                .answeredQuestions(answeredQuestions)
                .build();
    }
    
    // Helper method to map QuizAttempt to DetailedResponseDTO
    private QuizAttemptDTO.DetailedResponse mapToDetailedResponseDTO(QuizAttempt attempt, List<StudentAnswer> answers) {
        // Map student answers to DTOs
        List<StudentAnswerDTO.Response> answerDTOs = answers.stream()
                .map(this::mapToStudentAnswerResponseDTO)
                .collect(Collectors.toList());
        
        return QuizAttemptDTO.DetailedResponse.builder()
                .id(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .studentId(attempt.getStudent().getId())
                .studentName(attempt.getStudent().getFullName())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .score(attempt.getScore())
                .percentageScore(attempt.getPercentageScore())
                .passed(attempt.isPassed())
                .status(attempt.getStatus())
                .answers(answerDTOs)
                .build();
    }
    
    // Helper method to map StudentAnswer to ResponseDTO
    private StudentAnswerDTO.Response mapToStudentAnswerResponseDTO(StudentAnswer answer) {
        // Map selected options to DTOs
        List<AnswerOptionDTO.Response> selectedOptionDTOs = answer.getSelectedOptions().stream()
                .sorted(Comparator.comparing(AnswerOption::getOrderIndex))
                .map(option -> AnswerOptionDTO.Response.builder()
                        .id(option.getId())
                        .text(option.getText())
                        .isCorrect(option.isCorrect())
                        .feedback(option.getFeedback())
                        .orderIndex(option.getOrderIndex())
                        .questionId(option.getQuestion().getId())
                        .build())
                .collect(Collectors.toList());
        
        return StudentAnswerDTO.Response.builder()
                .id(answer.getId())
                .attemptId(answer.getAttempt().getId())
                .questionId(answer.getQuestion().getId())
                .questionText(answer.getQuestion().getText())
                .questionType(answer.getQuestion().getType().name())
                .selectedOptions(selectedOptionDTOs)
                .textAnswer(answer.getTextAnswer())
                .score(answer.getScore())
                .isCorrect(answer.isCorrect())
                .manuallyGraded(answer.isManuallyGraded())
                .instructorFeedback(answer.getInstructorFeedback())
                .build();
    }
}