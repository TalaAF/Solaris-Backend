package com.example.lms.notification.domain;


import com.example.lms.assignment.assignments.model.Assignment;
import com.example.lms.assignment.assignments.repository.AssignmentRepository;
import com.example.lms.assessment.model.Quiz;
import com.example.lms.assessment.repository.QuizRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.notification.service.NotificationService;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentNotificationService {
    
    private final NotificationService notificationService;
    private final AssignmentRepository assignmentRepository;
    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    /**
     * Notify students about assignment deadlines
     */
    @Transactional
    @Async
    public void notifyAssignmentDeadline(Long assignmentId, int hoursRemaining) {
        try {
            Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
            
            // Get the course and its enrolled students
            
            Course course = courseRepository.findById(assignment.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            List<User> students = course.getStudents().stream().collect(Collectors.toList());
            
            if (students.isEmpty()) {
                log.info("No students to notify for assignment deadline in course ID: {}", course.getId());
                return;
            }
            
            NotificationType type;
            switch (hoursRemaining) {
                case 24:
                    type = NotificationType.ASSIGNMENT_DEADLINE_24H;
                    break;
                case 12:
                    type = NotificationType.ASSIGNMENT_DEADLINE_12H;
                    break;
                case 1:
                    type = NotificationType.ASSIGNMENT_DEADLINE_1H;
                    break;
                default:
                    log.warn("Unsupported hoursRemaining value: {}", hoursRemaining);
                    return;
            }
            
            String title = "Assignment Due in " + hoursRemaining + " Hours";
            String content = "Your assignment '" + assignment.getTitle() + "' is due soon.";
            
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("assignmentTitle", assignment.getTitle());
            data.put("hoursRemaining", hoursRemaining);
            data.put("dueDate", assignment.getDueDate().toString());
            
            notificationService.createNotificationsForUsers(
                    type,
                    students,
                    title,
                    content,
                    assignmentId,
                    "assignment",
                    data
            );
            
            log.info("Sent assignment deadline notifications ({} hours) to {} students for assignment ID: {}", 
                    hoursRemaining, students.size(), assignmentId);
        } catch (Exception e) {
            log.error("Error sending assignment deadline notifications for assignment ID: " + assignmentId, e);
        }
    }
    
    /**
     * Notify students about quiz availability
     */
    @Transactional
    @Async
    public void notifyQuizAvailable(Long quizId, List<Long> studentIds) {
        try {
            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
            
            Course course = courseRepository.findById(quiz.getCourse().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            
            List<User> students = new ArrayList<>();
            if (studentIds != null && !studentIds.isEmpty()) {
                students = userRepository.findAllById(studentIds);
            } else {
                // If no specific students provided, notify all enrolled students
                students = course.getStudents().stream().collect(Collectors.toList());
            }
            
            if (students.isEmpty()) {
                log.info("No students to notify for quiz availability in course ID: {}", course.getId());
                return;
            }
            
            String title = "New Quiz Available: " + quiz.getTitle();
            String content = "A new quiz is available in your course: " + course.getTitle();
            
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("quizTitle", quiz.getTitle());
            data.put("startDate", quiz.getStartDate() != null ? quiz.getStartDate().toString() : "Now");
            data.put("endDate", quiz.getEndDate() != null ? quiz.getEndDate().toString() : "No end date");
            data.put("timeLimit", quiz.getTimeLimit() != null ? quiz.getTimeLimit() + " minutes" : "No time limit");
            
            notificationService.createNotificationsForUsers(
                    NotificationType.QUIZ_AVAILABLE,
                    students,
                    title,
                    content,
                    quizId,
                    "quiz",
                    data
            );
            
            log.info("Sent quiz availability notifications to {} students for quiz ID: {}", 
                    students.size(), quizId);
        } catch (Exception e) {
            log.error("Error sending quiz availability notifications for quiz ID: " + quizId, e);
        }
    }
    
    /**
     * Notify a student about posted grades
     */
    @Transactional
    @Async
    public void notifyGradePosted(Long courseId, Long studentId, String assessmentType, String assessmentTitle) {
        try {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
            
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
            
            String title = "Grade Posted: " + assessmentTitle;
            String content = "Your grade for " + assessmentTitle + " in " + course.getTitle() + " has been posted.";
            
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("assessmentTitle", assessmentTitle);
            data.put("assessmentType", assessmentType);
            
            notificationService.createNotification(
                    NotificationType.GRADE_POSTED,
                    student,
                    title,
                    content,
                    courseId,
                    "course",
                    data
            );
            
            log.info("Sent grade posted notification to student ID: {} for assessment: {} in course ID: {}", 
                    studentId, assessmentTitle, courseId);
        } catch (Exception e) {
            log.error("Error sending grade posted notification for student ID: " + studentId, e);
        }
    }
}