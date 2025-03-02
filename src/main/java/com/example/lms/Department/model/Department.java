package com.example.lms.Department.model;

import com.example.lms.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
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
    
    @OneToMany(mappedBy = "department")
    private Set<User> users = new HashSet<>();
    
    // For healthcare-specific functionality
    private String specialtyArea;
    private String headOfDepartment;
    private String contactInformation;
    private boolean isActive = true;
}