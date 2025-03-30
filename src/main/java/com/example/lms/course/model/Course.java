package com.example.lms.course.model;

import com.example.lms.Department.model.Department;
import com.example.lms.assessment.model.Quiz;
import com.example.lms.common.BaseEntity;
import com.example.lms.content.model.Content;
import com.example.lms.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * Course entity representing a course in the LMS.
 * Contains relationships to instructors, students, content, quizzes, and departments.
 * Extends BaseEntity to inherit id, createdAt, and updatedAt fields.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "courses")
@EqualsAndHashCode(callSuper = true)
public class Course extends BaseEntity {
    private String name;
    @Column(nullable = false, unique = true)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @ManyToMany
    @JoinTable(
        name = "course_students", 
        joinColumns = @JoinColumn(name = "course_id"), 
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<User> students = new HashSet<>();

    // Add Prerequisites (self-referencing ManyToMany relationship)
    @ManyToMany
    @JoinTable(
        name = "course_prerequisites", 
        joinColumns = @JoinColumn(name = "course_id"), 
        inverseJoinColumns = @JoinColumn(name = "prerequisite_course_id")
    )
    private Set<Course> prerequisites = new HashSet<>();
    
    // Courses that have this course as a prerequisite
    @ManyToMany(mappedBy = "prerequisites")
    @JsonIgnore
    private Set<Course> subsequentCourses = new HashSet<>();

    @Column(nullable = true)
    private Integer maxCapacity;
    
    // Course availability dates
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    // Course status
    private boolean published = true;
    private boolean archived = true;
    
    // Course settings
    private boolean enrollmentEnabled = true;
    private boolean contentVisible = true;
    private boolean gradingEnabled = true;
    
    // Course tags and metadata
    @ElementCollection
    @CollectionTable(name = "course_tags", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();
    
    @Column(columnDefinition = "TEXT")
    private String learningObjectives;
    
    private Integer creditHours;
    private String academicLevel;
    
    @Enumerated(EnumType.STRING)
    private CourseStatus status = CourseStatus.DRAFT;

    // Inverse relationship with content
    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Content> contents = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes = new ArrayList<>();
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompletionRequirement> completionRequirements = new ArrayList<>();
    
    /**
     * Get the current number of students enrolled in the course
     * 
     * @return Current enrollment count
     */
    public int getCurrentEnrollment() {
        return this.students.size();
    }
    
    /**
     * Check if the course has capacity for more students
     * 
     * @return true if there's capacity, false if at maximum
     */
    public boolean hasCapacity() {
        return maxCapacity == null || getCurrentEnrollment() < maxCapacity;
    }
    
    /**
     * Get remaining capacity
     * 
     * @return Number of remaining slots, or null if no capacity limit
     */
    public Integer getRemainingCapacity() {
        if (maxCapacity == null) {
            return null;
        }
        return maxCapacity - getCurrentEnrollment();
    }
    
    /**
     * Check if course is currently active (within date range)
     * 
     * @return true if course is active
     */
    public boolean isActive() {
        if (!published || archived) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if a student is enrolled in this course
     * 
     * @param student User to check
     * @return true if enrolled, false otherwise
     */
    public boolean hasStudent(User student) {
        return students.contains(student);
    }
    
    /**
     * Add a student to the course
     * 
     * @param student User to add
     * @return true if added, false if already enrolled or course is full
     */
    public boolean addStudent(User student) {
        if (hasStudent(student) || !hasCapacity()) {
            return false;
        }
        return students.add(student);
    }
    
    /**
     * Remove a student from the course
     * 
     * @param student User to remove
     * @return true if removed, false if not enrolled
     */
    public boolean removeStudent(User student) {
        return students.remove(student);
    }
    
    /**
     * Add a prerequisite course
     * 
     * @param course Course to add as prerequisite
     * @return true if added, false if already a prerequisite
     */
    public boolean addPrerequisite(Course course) {
        return prerequisites.add(course);
    }
    
    /**
     * Remove a prerequisite course
     * 
     * @param course Course to remove from prerequisites
     * @return true if removed, false if not a prerequisite
     */
    public boolean removePrerequisite(Course course) {
        return prerequisites.remove(course);
    }
    
    /**
     * Add a tag to the course
     * 
     * @param tag Tag to add
     * @return true if added, false if already exists
     */
    public boolean addTag(String tag) {
        return tags.add(tag.toLowerCase());
    }
    
    /**
     * Remove a tag from the course
     * 
     * @param tag Tag to remove
     * @return true if removed, false if not present
     */
    public boolean removeTag(String tag) {
        return tags.remove(tag.toLowerCase());
    }
}