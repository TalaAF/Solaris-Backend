// src/main/java/com/example/lms/user/model/User.java
package com.example.lms.user.model;

import com.example.lms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import com.example.lms.user.model.Role;

@Data                   // Lombok: Generates getters, setters, toString, equals, and hashCode
@EqualsAndHashCode(callSuper = true) // Lombok: Generates equals and hashCode with a call to superclass
@Table(name = "users")   // Specifies the database table name                  // Lombok: Generates getters, setters, toString, equals, and hashCode
@Builder                // Lombok: Enables the builder pattern for object creation
@NoArgsConstructor     // Lombok: Generates a no-args constructor
@AllArgsConstructor    // Lombok: Generates a constructor with all properties
@Entity
public class User extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String email;        // User's email address (used for login)
    
    @Column(nullable = false)
    private String password;     // Encrypted password
    
    @Column(nullable = false)
    private String fullName;     // User's full name
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;          // User's role in the system
    
    private String profilePicture;  // Optional profile picture URL
    
    @Column(nullable = false)
    private boolean isActive = true;  // Account status flag
}