package com.example.lms.assignment.dashboard.service;

import com.example.lms.assignment.assessment.model.Assignment;
import com.example.lms.assignment.assessment.repository.AssignmentRepository;
import com.example.lms.assignment.dashboard.model.CourseProgress;
import com.example.lms.assignment.dashboard.model.StudentDashboard;
import com.example.lms.assignment.dashboard.model.UpcomingDeadline;
import com.example.lms.assignment.forum.model.ForumPost;
import com.example.lms.assignment.forum.repository.ForumPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private ForumPostRepository forumPostRepository;

    public StudentDashboard getStudentDashboard(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID must be provided");
        }

        StudentDashboard dashboard = new StudentDashboard();
        dashboard.setStudentId(studentId);

        // Fetch upcoming deadlines (assignments only)
        List<UpcomingDeadline> deadlines = new ArrayList<>();

        List<Assignment> assignments = assignmentRepository.findAll().stream()
                .filter(assignment -> assignment.getDueDate().isAfter(LocalDateTime.now())
                        && assignment.getDueDate().isBefore(LocalDateTime.now().plusDays(7)))
                .collect(Collectors.toList());

        for (Assignment assignment : assignments) {
            UpcomingDeadline deadline = new UpcomingDeadline();
            deadline.setAssessmentId(assignment.getId());
            deadline.setAssessmentType("ASSIGNMENT");
            deadline.setTitle(assignment.getTitle());
            deadline.setDueDate(assignment.getDueDate());
            deadlines.add(deadline);
        }

        dashboard.setUpcomingDeadlines(deadlines);

        // Fetch course progress (placeholder)
        List<CourseProgress> progressList = new ArrayList<>();
        CourseProgress progress = new CourseProgress();
        progress.setCourseId(1L);
        progress.setCourseName("Sample Course");
        progress.setCompletionPercentage(75.0);
        progressList.add(progress);
        dashboard.setCourseProgress(progressList);

        // Fetch recent forum activity
        List<ForumPost> recentPosts = forumPostRepository.findAll().stream()
                .filter(post -> true) // Placeholder for enrollment check
                .sorted((p1, p2) -> p2.getPostedDate().compareTo(p1.getPostedDate()))
                .limit(5)
                .collect(Collectors.toList());

        dashboard.setRecentForumActivity(recentPosts);

        return dashboard;
    }
}