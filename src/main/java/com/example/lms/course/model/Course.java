package com.example.lms.course.model;

import com.example.lms.Department.model.Department;
import com.example.lms.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

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

    // Add Max Capacity field
    @Column(nullable = false)
    private Integer maxCapacity;

    // Get the current number of students enrolled in the course
    public int getCurrentEnrollment() {
        return this.students.size();
    }

    public String getTitle() {
        return this.name; // Return the name as the title
    }

    public void setTitle(String title) {
        this.name = title; // Set the name as the title
    }
}
