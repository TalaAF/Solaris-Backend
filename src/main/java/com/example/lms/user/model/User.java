// src/main/java/com/example/lms/user/model/User.java
package com.example.lms.user.model;

import com.example.lms.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.lms.Department.model.Department;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import com.example.lms.security.model.Role;

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

    @Column(nullable = false)
    private String password; // Encrypted password

    @Column(nullable = false)
    private String fullName; // User's full name

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    private String profilePicture; // Optional profile picture URL

    @Column(nullable = false)
    private boolean isActive = true; // Account status flag

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department; // Relationship with Department

   
    /**
     * Check if the raw password matches the stored encoded password
     * 
     * @param rawPassword The raw password entered by the user
     * @param passwordEncoder The PasswordEncoder to validate the password
     * @return true if the passwords match, false otherwise
     */
    public boolean checkPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.password);
    }
}