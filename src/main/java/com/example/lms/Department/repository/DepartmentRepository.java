package com.example.lms.Department.repository;

import com.example.lms.Department.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
    Optional<Department> findByCode(String code);
    List<Department> findByIsActiveTrue();
    boolean existsByName(String name);
    boolean existsByCode(String code);
}