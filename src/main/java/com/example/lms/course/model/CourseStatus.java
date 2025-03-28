package com.example.lms.course.model;

/**
 * Enum representing the various states a course can be in.
 * Used to track course lifecycle from creation to archival.
 */
public enum CourseStatus {
    /**
     * Course is in draft mode, being prepared but not yet ready for students
     */
    DRAFT,
    
    /**
     * Course is published and active, students can enroll and access content
     */
    PUBLISHED,
    
    /**
     * Course has been published but is temporarily unavailable
     */
    SUSPENDED,
    
    /**
     * Course registration is closed, but existing students can still access content
     */
    CLOSED,
    
    /**
     * Course has ended and is now in read-only mode
     */
    COMPLETED, 
    
    /**
     * Course is archived and only accessible to administrators
     */
    ARCHIVED
}