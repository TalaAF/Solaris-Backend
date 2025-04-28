package com.example.lms.content.model;

/**
 * Enum representing the status of a module
 * 
 * DRAFT - Module is being created/edited and is not visible to students
 * PUBLISHED - Module is visible to students (subject to release conditions)
 * ARCHIVED - Module is not active and hidden from regular view
 */
public enum ModuleStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}