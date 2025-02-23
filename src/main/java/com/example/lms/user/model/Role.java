// src/main/java/com/example/lms/user/model/Role.java
package com.example.lms.user.model;

// This enum defines the possible roles a user can have in our system
public enum Role {
    ADMIN,          // Can manage everything in the system
    INSTRUCTOR,     // Can create and manage courses
    STUDENT         // Can enroll in and access courses
}