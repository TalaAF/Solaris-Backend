package com.example.lms.assessment.service;

import com.example.lms.assessment.dto.QuestionDTO;
import com.example.lms.assessment.dto.QuizDTO;
import com.example.lms.assessment.model.AttemptStatus;
import com.example.lms.assessment.model.Quiz;
import com.example.lms.assessment.repository.QuestionRepository;
import com.example.lms.assessment.repository.QuizAttemptRepository;
import com.example.lms.assessment.repository.QuizRepository;
import com.example.lms.common.Exception.ResourceAlreadyExistsException;
import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionService questionService;

    @Override
    @Transactional
    public QuizDTO.Response createQuiz(QuizDTO.Request quizDTO) {
        // Check if a quiz with the same title already exists in the course
        if (quizRepository.existsByTitleAndCourseIdAndIdNot(quizDTO.getTitle(), quizDTO.getCourseId(), null)) {
            throw new ResourceAlreadyExistsException("Quiz with title '" + quizDTO.getTitle() + "' already exists in this course");
        }

        // Fetch the course
        Course course = courseRepository.findById(quizDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + quizDTO.getCourseId()));

        // Create the quiz entity
        Quiz quiz = Quiz.builder()
                .title(quizDTO.getTitle())
                .description(quizDTO.getDescription())
                .timeLimit(quizDTO.getTimeLimit())
                .startDate(quizDTO.getStartDate())
                .endDate(quizDTO.getEndDate())
                .passingScore(quizDTO.getPassingScore() != null ? quizDTO.getPassingScore() : 60.0) // Default 60% passing score
                .randomizeQuestions(quizDTO.isRandomizeQuestions())
                .published(quizDTO.isPublished())
                .course(course)
                .build();

        // Save the quiz
        Quiz savedQuiz = quizRepository.save(quiz);

        // Map to response DTO
        return mapToResponseDTO(savedQuiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDTO.Response getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        
        return mapToResponseDTO(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDTO.DetailedResponse getQuizWithQuestions(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        
        // Get questions for the quiz
        List<QuestionDTO.Response> questions = questionService.getQuestionsByQuizId(id);
        
        return mapToDetailedResponseDTO(quiz, questions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO.Response> getQuizzesByCourse(Long courseId) {
        // Check if course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        
        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        return quizzes.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO.Response> getPublishedQuizzesByCourse(Long courseId) {
        // Check if course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        
        List<Quiz> quizzes = quizRepository.findByCourseIdAndPublishedTrue(courseId);
        return quizzes.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO.Response> getAvailableQuizzesByCourse(Long courseId) {
        // Check if course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<Quiz> quizzes = quizRepository.findAvailableQuizzesByCourseId(courseId, now);
        return quizzes.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDTO.StudentView getQuizForStudent(Long quizId, Long studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        
        // Check if quiz is published
        if (!quiz.isPublished()) {
            throw new ResourceNotFoundException("Quiz not found or not available");
        }
        
        // Check if quiz is within available dates
        LocalDateTime now = LocalDateTime.now();
        boolean isAvailable = (quiz.getStartDate() == null || !now.isBefore(quiz.getStartDate())) &&
                             (quiz.getEndDate() == null || !now.isAfter(quiz.getEndDate()));
                             
        if (!isAvailable) {
            throw new ResourceNotFoundException("Quiz not found or not available");
        }
        
        // Get student's attempts for this quiz
        List<Double> scores = quizAttemptRepository.findByQuizIdAndStudentId(quizId, studentId).stream()
                .filter(attempt -> attempt.getStatus() == AttemptStatus.COMPLETED)
                .map(attempt -> attempt.getPercentageScore())
                .collect(Collectors.toList());
        
        boolean attempted = !scores.isEmpty();
        boolean completed = attempted;
        Double highestScore = scores.stream().max(Double::compare).orElse(null);
        boolean passed = highestScore != null && highestScore >= quiz.getPassingScore();
        
        return QuizDTO.StudentView.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .timeLimit(quiz.getTimeLimit())
                .startDate(quiz.getStartDate())
                .endDate(quiz.getEndDate())
                .passingScore(quiz.getPassingScore())
                .courseId(quiz.getCourse().getId())
                .courseName(quiz.getCourse().getTitle())
                .questionCount(quiz.getQuestions().size())
                .totalPossibleScore(quiz.getTotalPossibleScore())
                .attempted(attempted)
                .completed(completed)
                .highestScore(highestScore)
                .passed(passed)
                .build();
    }

    @Override
    @Transactional
    public QuizDTO.Response updateQuiz(Long id, QuizDTO.Request quizDTO) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        
        // Check if title is unique in course (excluding this quiz)
        if (!quiz.getTitle().equals(quizDTO.getTitle()) && 
            quizRepository.existsByTitleAndCourseIdAndIdNot(quizDTO.getTitle(), quizDTO.getCourseId(), id)) {
            throw new ResourceAlreadyExistsException("Quiz with title '" + quizDTO.getTitle() + "' already exists in this course");
        }
        
        // Check if course exists if changing course
        if (!quiz.getCourse().getId().equals(quizDTO.getCourseId())) {
            Course newCourse = courseRepository.findById(quizDTO.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + quizDTO.getCourseId()));
            quiz.setCourse(newCourse);
        }
        
        // Update quiz fields
        quiz.setTitle(quizDTO.getTitle());
        quiz.setDescription(quizDTO.getDescription());
        quiz.setTimeLimit(quizDTO.getTimeLimit());
        quiz.setStartDate(quizDTO.getStartDate());
        quiz.setEndDate(quizDTO.getEndDate());
        quiz.setPassingScore(quizDTO.getPassingScore());
        quiz.setRandomizeQuestions(quizDTO.isRandomizeQuestions());
        quiz.setPublished(quizDTO.isPublished());
        
        // Save updated quiz
        Quiz updatedQuiz = quizRepository.save(quiz);
        
        return mapToResponseDTO(updatedQuiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        
        // Delete all questions (will cascade to answer options)
        questionRepository.deleteByQuizId(id);
        
        // Delete the quiz
        quizRepository.delete(quiz);
    }

    @Override
    @Transactional
    public QuizDTO.Response publishQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        
        // Check if quiz has questions
        if (quiz.getQuestions().isEmpty()) {
            throw new IllegalStateException("Cannot publish a quiz with no questions");
        }
        
        quiz.setPublished(true);
        Quiz updatedQuiz = quizRepository.save(quiz);
        
        return mapToResponseDTO(updatedQuiz);
    }

    @Override
    @Transactional
    public QuizDTO.Response unpublishQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        
        quiz.setPublished(false);
        Quiz updatedQuiz = quizRepository.save(quiz);
        
        return mapToResponseDTO(updatedQuiz);
    }
    
    // Helper method to map Quiz entity to QuizDTO.Response
    private QuizDTO.Response mapToResponseDTO(Quiz quiz) {
        int questionCount = quiz.getQuestions().size();
        int totalPossibleScore = quiz.getTotalPossibleScore();
        
        return QuizDTO.Response.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .timeLimit(quiz.getTimeLimit())
                .startDate(quiz.getStartDate())
                .endDate(quiz.getEndDate())
                .passingScore(quiz.getPassingScore())
                .randomizeQuestions(quiz.isRandomizeQuestions())
                .published(quiz.isPublished())
                .courseId(quiz.getCourse().getId())
                .courseName(quiz.getCourse().getTitle())
                .questionCount(questionCount)
                .totalPossibleScore(totalPossibleScore)
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }
    
    // Helper method to map Quiz entity to QuizDTO.DetailedResponse
    private QuizDTO.DetailedResponse mapToDetailedResponseDTO(Quiz quiz, List<QuestionDTO.Response> questions) {
        return QuizDTO.DetailedResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .timeLimit(quiz.getTimeLimit())
                .startDate(quiz.getStartDate())
                .endDate(quiz.getEndDate())
                .passingScore(quiz.getPassingScore())
                .randomizeQuestions(quiz.isRandomizeQuestions())
                .published(quiz.isPublished())
                .courseId(quiz.getCourse().getId())
                .courseName(quiz.getCourse().getTitle())
                .totalPossibleScore(quiz.getTotalPossibleScore())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .questions(questions)
                .build();
    }
}