package com.example.lms.assignment.dashboard.mapper;

import com.example.lms.assignment.dashboard.dto.StudentDashboardDTO;
import com.example.lms.assignment.dashboard.dto.UpcomingDeadlineDTO;
import com.example.lms.assignment.dashboard.dto.CourseProgressDTO;
import com.example.lms.assignment.dashboard.model.StudentDashboard;
import com.example.lms.assignment.dashboard.model.UpcomingDeadline;
import com.example.lms.assignment.dashboard.model.CourseProgress;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DashboardMapper {
    StudentDashboardDTO toStudentDashboardDTO(StudentDashboard studentDashboard);
    StudentDashboard toStudentDashboard(StudentDashboardDTO studentDashboardDTO);

    UpcomingDeadlineDTO toUpcomingDeadlineDTO(UpcomingDeadline upcomingDeadline);
    UpcomingDeadline toUpcomingDeadline(UpcomingDeadlineDTO upcomingDeadlineDTO);

    CourseProgressDTO toCourseProgressDTO(CourseProgress courseProgress);
    CourseProgress toCourseProgress(CourseProgressDTO courseProgressDTO);
}