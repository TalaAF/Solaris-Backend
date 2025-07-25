package com.example.lms.user.repository;

import com.example.lms.Department.model.Department;
import com.example.lms.course.model.Course;
import com.example.lms.security.model.Role;
import com.example.lms.user.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository("UserRepository")
public interface UserRepository extends JpaRepository<User, Long> {
  
    // Method to find a user by their email
    Optional<User> findByEmail(String email);

    // Custom method to check if an email is already registered
    boolean existsByEmail(String email);

    Page<User> findAll(Specification<User> spec, Pageable pageable);
    long countByIsActiveTrue();
    long countByRolesContaining(Role role);
    long countByDepartment(Department department);
    long countByCreatedAtAfter(LocalDateTime date);

    // Add this method to find users by role name
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    // Count users in a specific department
    Long countByDepartmentId(Long departmentId);
    
    // Count users in all departments (returns departmentId, count)
    @Query("SELECT u.department.id, COUNT(u) FROM User u WHERE u.department IS NOT NULL GROUP BY u.department.id")
    List<Object[]> countUsersByDepartment();
}
