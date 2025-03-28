package com.example.lms.enrollment.service;

import com.example.lms.notification.model.NotificationType;
import com.example.lms.notification.service.NotificationService;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnrollmentNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentNotificationService.class);

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    /**
     * Notify student and instructor about enrollment
     */
    public void notifyEnrollment(Long studentId, Long courseId) {
        try {
            User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
            
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
            
            // Create notification data
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("courseId", course.getId());
            data.put("instructorName", course.getInstructor().getFullName());
            
            // Send notification to student
            notificationService.createNotification(
                NotificationType.COURSE_CONTENT_UPLOAD, // Use appropriate type from your enum
                student, 
                "Enrollment Successful", 
                "You have been successfully enrolled in " + course.getTitle(), 
                courseId, 
                "course", 
                data
            );
            
            // Optionally, also notify the instructor
            notificationService.createNotification(
                NotificationType.COURSE_ANNOUNCEMENT, // Use appropriate type from your enum
                course.getInstructor(),
                "New Student Enrolled",
                student.getFullName() + " has enrolled in your course " + course.getTitle(),
                courseId,
                "course",
                data
            );
            
            logger.debug("Enrollment notifications sent for student ID: {} in course ID: {}", studentId, courseId);
        } catch (Exception e) {
            logger.error("Failed to send enrollment notification", e);
        }
    }
    
    /**
     * Notify student and instructor about course completion
     */
    public void notifyCourseCompletion(Long studentId, Long courseId) {
        try {
            User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
            
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
            
            // Create notification data
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("courseId", course.getId());
            data.put("instructorName", course.getInstructor().getFullName());
            
            // Send completion notification to student
            notificationService.createNotification(
                NotificationType.GRADE_POSTED, // Use appropriate type from your enum
                student, 
                "Course Completed", 
                "Congratulations! You have successfully completed " + course.getTitle(), 
                courseId, 
                "course", 
                data
            );
            
            // Notify the instructor
            notificationService.createNotification(
                NotificationType.COURSE_ANNOUNCEMENT, // Use appropriate type from your enum
                course.getInstructor(),
                "Student Completed Course",
                student.getFullName() + " has completed your course " + course.getTitle(),
                courseId,
                "course",
                data
            );
            
            logger.debug("Course completion notifications sent for student ID: {} in course ID: {}", studentId, courseId);
        } catch (Exception e) {
            logger.error("Failed to send course completion notification", e);
        }
    }
    
    /**
     * Notify about upcoming deadlines
     */
    public void notifyDeadline(Long studentId, Long courseId, String itemName, String dueDate) {
        try {
            User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
            
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
            
            // Create notification data
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("itemName", itemName);
            data.put("dueDate", dueDate);
            
            notificationService.createNotification(
                NotificationType.ASSIGNMENT_DEADLINE_24H, // Use appropriate type from your enum
                student, 
                "Upcoming Deadline: " + itemName,
                "Reminder: " + itemName + " for " + course.getTitle() + " is due on " + dueDate,
                courseId,
                "course",
                data
            );
            
            logger.debug("Deadline notification sent to student ID: {} for item: {} in course ID: {}", 
                        studentId, itemName, courseId);
        } catch (Exception e) {
            logger.error("Failed to send deadline notification", e);
        }
    }
    
    /**
     * Notify about progress milestones
     */
    public void notifyProgressMilestone(Long studentId, Long courseId, double progress) {
        try {
            // Only notify at significant milestones (25%, 50%, 75%)
            if (progress != 25.0 && progress != 50.0 && progress != 75.0) {
                return;
            }
            
            User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
            
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
            
            // Create notification data
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("progress", progress);
            
            notificationService.createNotification(
                NotificationType.COURSE_ANNOUNCEMENT, // Use appropriate type from your enum
                student,
                "Progress Milestone Reached",
                "You have completed " + progress + "% of " + course.getTitle(),
                courseId,
                "course",
                data
            );
            
            logger.debug("Progress milestone notification sent to student ID: {} for {}% completion in course ID: {}", 
                        studentId, progress, courseId);
        } catch (Exception e) {
            logger.error("Failed to send progress milestone notification", e);
        }
    }
}