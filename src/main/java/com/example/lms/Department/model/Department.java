package com.example.lms.Department.model;

import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String code;
 
    @JsonIgnore
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "department")
    private Set<User> users = new HashSet<>();

    // For healthcare-specific functionality
    private String specialtyArea;
    private String contactInformation;
    private boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "head_id")
    private User head;

    // getters and setters
    public User getHead() {
        return head;
    }
    
    public void setHead(User head) {
        this.head = head;
    }
}