// src/main/java/com/example/lms/user/model/User.java
package com.example.lms.user.model;

import com.example.lms.common.BaseEntity;
import com.example.lms.enrollment.model.Enrollment;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.lms.Department.model.Department;
import com.example.lms.assessment.model.QuizAttempt;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import java.util.*;
import com.example.lms.security.model.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data // Lombok: Generates getters, setters, toString, equals, and hashCode
@EqualsAndHashCode(callSuper = true) // Lombok: Generates equals and hashCode with a call to superclass
@Table(name = "users") // Specifies the database table name // Lombok: Generates getters, setters,
                       // toString, equals, and hashCode
@Builder // Lombok: Enables the builder pattern for object creation
@NoArgsConstructor // Lombok: Generates a no-args constructor
@AllArgsConstructor // Lombok: Generates a constructor with all properties
@Entity
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email; // User's email address (used for login)

    @JsonIgnore // Don't expose password in JSON responses
    @Column(nullable = false)
    private String password; // Encrypted password

    @Column(nullable = false)
    private String fullName; // User's full name

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore // Don't expose quiz attempts in user JSON
    @OneToMany(mappedBy = "student")
    private List<QuizAttempt> quizAttempts = new ArrayList<>();

    @JsonIgnore // Don't expose enrollments in user JSON
    @OneToMany(mappedBy = "student")
    private Set<Enrollment> enrollments = new HashSet<>();

    // In User.java
    @Column
    @JsonIgnore // Don't expose token version in JSON
    private Long tokenVersion = 0L;

    public void incrementTokenVersion() {
        this.tokenVersion++;
    }

    private String profilePicture; // Optional profile picture URL

    @Column(nullable = false)
    private boolean isActive = true; // Account status flag

    // Add deleted flag for soft deletion
    @Column(nullable = false)
    @JsonIgnore
    private boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department; // Relationship with Department

    /**
     * Check if the raw password matches the stored encoded password
     * 
     * @param rawPassword     The raw password entered by the user
     * @param passwordEncoder The PasswordEncoder to validate the password
     * @return true if the passwords match, false otherwise
     */
    public boolean checkPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.password);
    }
    
    /**
     * Gets the user's name as expected by frontend.
     * This method helps with API compatibility.
     * 
     * @return The user's full name
     */
    public String getName() {
        return this.fullName;
    }
    
    /**
     * Gets the user's profile image as expected by frontend.
     * This method helps with API compatibility.
     * 
     * @return The user's profile picture URL
     */
    public String getProfileImage() {
        return this.profilePicture;
    }

    /**
     * Checks if the user has been soft deleted
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    /**
     * Sets the deleted status of the user
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}