package com.example.lms.notification.model;

public enum NotificationType {
    COURSE_CONTENT_UPLOAD("New course content", "Announcements"),
    ASSIGNMENT_DEADLINE_24H("Assignment due in 24 hours", "Reminders"),
    ASSIGNMENT_DEADLINE_12H("Assignment due in 12 hours", "Reminders"),
    ASSIGNMENT_DEADLINE_1H("Assignment due in 1 hour", "Reminders"),
    QUIZ_AVAILABLE("Quiz available", "Academic"),
    GRADE_POSTED("Grade posted", "Academic"),
    COURSE_ANNOUNCEMENT("Course announcement", "Announcements"),
    FORUM_REPLY("Forum reply", "Announcements"),
    FORUM_MENTION("Mentioned in forum", "Announcements");
    
    private final String displayName;
    private final String category;
    
    NotificationType(String displayName, String category) {
        this.displayName = displayName;
        this.category = category;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getCategory() {
        return category;
    }
}