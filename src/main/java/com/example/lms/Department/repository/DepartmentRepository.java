package com.example.lms.Department.repository;

import com.example.lms.Department.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // Add pagination support
    Page<Department> findByIsActiveTrue(Pageable pageable);
    
    // New search methods
    @Query("SELECT d FROM Department d WHERE " +
           "LOWER(d.name) LIKE :keyword OR " +
           "LOWER(d.code) LIKE :keyword OR " +
           "LOWER(d.specialtyArea) LIKE :keyword")
    Page<Department> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT d FROM Department d WHERE " +
           "(LOWER(d.name) LIKE :keyword OR " +
           "LOWER(d.code) LIKE :keyword OR " +
           "LOWER(d.specialtyArea) LIKE :keyword) AND " +
           "d.isActive = true")
    Page<Department> searchActiveByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // Count methods
    long countByIsActiveTrue();
    
    long countByIsActiveFalse();
}