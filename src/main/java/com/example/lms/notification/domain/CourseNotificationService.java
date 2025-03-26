package com.example.lms.notification.domain;

import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.notification.service.NotificationService;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
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
public class CourseNotificationService {
    
    private final NotificationService notificationService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    /**
     * Notify students about new course content
     */
    @Transactional
    @Async
    public void notifyCourseContentUpload(Long courseId, String contentTitle, List<Long> studentIds) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
            
            List<User> students = new ArrayList<>();
            if (studentIds != null && !studentIds.isEmpty()) {
                students = userRepository.findAllById(studentIds);
            } else {
                // If no specific students provided, notify all enrolled students
                students = course.getStudents().stream().collect(Collectors.toList());
            }
            
            if (students.isEmpty()) {
                log.info("No students to notify for course content upload in course ID: {}", courseId);
                return;
            }
            
            String title = "New Content: " + contentTitle;
            String content = "New content has been added to your course: " + course.getTitle();
            
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("contentTitle", contentTitle);
            data.put("instructorName", course.getInstructor().getFullName());
            
            notificationService.createNotificationsForUsers(
                    NotificationType.COURSE_CONTENT_UPLOAD,
                    students,
                    title,
                    content,
                    courseId,
                    "course",
                    data
            );
            
            log.info("Sent course content upload notifications to {} students for course ID: {}", 
                    students.size(), courseId);
        } catch (Exception e) {
            log.error("Error sending course content upload notifications for course ID: " + courseId, e);
        }
    }

    /**
     * Notify students about course announcements
     */
    @Transactional
    @Async
    public void notifyCourseAnnouncement(Long courseId, String announcementTitle, String announcementContent) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
            
            List<User> students = course.getStudents().stream().collect(Collectors.toList());
            
            if (students.isEmpty()) {
                log.info("No students to notify for course announcement in course ID: {}", courseId);
                return;
            }
            
            String title = "Announcement: " + announcementTitle;
            String content = announcementContent;
            
            Map<String, Object> data = new HashMap<>();
            data.put("courseName", course.getTitle());
            data.put("announcementTitle", announcementTitle);
            data.put("announcementContent", announcementContent);
            data.put("instructorName", course.getInstructor().getFullName());
            
            notificationService.createNotificationsForUsers(
                    NotificationType.COURSE_ANNOUNCEMENT,
                    students,
                    title,
                    content,
                    courseId,
                    "course",
                    data
            );
            
            log.info("Sent course announcement notifications to {} students for course ID: {}", 
                    students.size(), courseId);
        } catch (Exception e) {
            log.error("Error sending course announcement notifications for course ID: " + courseId, e);
        }
    }
}