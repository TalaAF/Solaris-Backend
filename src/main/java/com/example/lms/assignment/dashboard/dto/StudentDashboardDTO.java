package com.example.lms.assignment.dashboard.dto;

import com.example.lms.assignment.forum.dto.ForumPostDTO;
import lombok.Data;

import java.util.List;

@Data
public class StudentDashboardDTO {
    private Long studentId;
    private List<UpcomingDeadlineDTO> upcomingDeadlines;
    private List<CourseProgressDTO> courseProgress;
    private List<ForumPostDTO> recentForumActivity;
}