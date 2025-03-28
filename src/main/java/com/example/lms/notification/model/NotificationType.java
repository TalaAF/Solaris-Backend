package com.example.lms.notification.model;

public enum NotificationType {
    COURSE_CONTENT_UPLOAD("New course content"),
    ASSIGNMENT_DEADLINE_24H("Assignment due in 24 hours"),
    ASSIGNMENT_DEADLINE_12H("Assignment due in 12 hours"),
    ASSIGNMENT_DEADLINE_1H("Assignment due in 1 hour"),
    QUIZ_AVAILABLE("Quiz available"),
    GRADE_POSTED("Grade posted"),
    COURSE_ANNOUNCEMENT("Course announcement"),
    FORUM_REPLY("Forum reply"),
    FORUM_MENTION("Mentioned in forum");
    
    private final String displayName;
    
    NotificationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}