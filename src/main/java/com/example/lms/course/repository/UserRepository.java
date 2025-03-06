package com.example.lms.course.repository;

import com.example.lms.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository("courseUserRepository")
public interface UserRepository extends JpaRepository<User, Long> {
    // repository methods


    Optional<User> findByEmail(String email);  // Changed from findByUsername to findByEmail
}
