// src/main/java/com/example/lms/user/repository/UserRepository.java
package com.example.lms.user.repository;

import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("userUserRepository")
public interface UserRepository extends JpaRepository<User, Long> {
  
    Optional<User> findByEmail(String email);
    
    // Custom method to check if an email is already registered
    boolean existsByEmail(String email);

    Optional<Course> findByUsername(String instructor);
}