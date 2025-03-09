package com.example.lms.course.repository;

import com.example.lms.Department.model.Department;
import com.example.lms.course.model.Course;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByName(String name);

    List<Course> findByDepartment(Department department);
}
