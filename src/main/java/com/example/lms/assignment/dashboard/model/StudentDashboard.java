package com.example.lms.assignment.dashboard.model;

import com.example.lms.assignment.forum.model.ForumPost;
import lombok.Data;

import java.util.List;

@Data
public class StudentDashboard {
    private Long studentId;
    private List<UpcomingDeadline> upcomingDeadlines;
    private List<CourseProgress> courseProgress;
    private List<ForumPost> recentForumActivity;
}