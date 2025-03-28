package com.example.lms.course.repository;

import com.example.lms.Department.model.Department;
import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Course entity providing data access methods
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    /**
     * Check if a course exists by name
     * 
     * @param name Course name
     * @return true if exists, false otherwise
     */
    boolean existsByTitle(String title);
    
    /**
     * Find courses by department
     * 
     * @param department Department entity
     * @return List of courses
     */
    List<Course> findByDepartment(Department department);
    
    /**
     * Find courses by instructor
     * 
     * @param instructor User entity representing instructor
     * @return List of courses
     */
    List<Course> findByInstructor(User instructor);
    
    /**
     * Find courses by department ID
     * 
     * @param departmentId Department ID
     * @return List of courses
     */
    @Query("SELECT c FROM Course c WHERE c.department.id = :departmentId")
    List<Course> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    /**
     * Find courses by student ID (courses where student is enrolled)
     * 
     * @param studentId Student ID
     * @return List of courses
     */
    @Query("SELECT c FROM Course c JOIN c.students s WHERE s.id = :studentId")
    List<Course> findByStudentId(@Param("studentId") Long studentId);
    
    /**
     * Find courses by keyword in title or description
     * 
     * @param keyword Search keyword
     * @param pageable Pagination information
     * @return Page of courses
     */
    @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Course> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Count courses by department
     * 
     * @param departmentId Department ID
     * @return Number of courses
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.department.id = :departmentId")
    Long countByDepartmentId(@Param("departmentId") Long departmentId);
    
    /**
     * Find course by title and department ID
     * 
     * @param title Course title
     * @param departmentId Department ID
     * @return Optional of Course
     */
    @Query("SELECT c FROM Course c WHERE c.title = :title AND c.department.id = :departmentId")
    Optional<Course> findByTitleAndDepartmentId(@Param("title") String title, @Param("departmentId") Long departmentId);
    
    /**
     * Check if a course has any enrolled students
     * 
     * @param courseId Course ID
     * @return true if has students, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Course c JOIN c.students s WHERE c.id = :courseId")
    boolean hasStudents(@Param("courseId") Long courseId);
    
    /**
     * Check if a course has any content
     * 
     * @param courseId Course ID
     * @return true if has content, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(ct) > 0 THEN true ELSE false END FROM Course c JOIN c.contents ct WHERE c.id = :courseId")
    boolean hasContent(@Param("courseId") Long courseId);
    
    /**
     * Check if a course has any quizzes
     * 
     * @param courseId Course ID
     * @return true if has quizzes, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(q) > 0 THEN true ELSE false END FROM Course c JOIN c.quizzes q WHERE c.id = :courseId")
    boolean hasQuizzes(@Param("courseId") Long courseId);
}