package com.example.lms.progress.repository;

import com.example.lms.progress.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Optional<Progress> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Progress> findByStudentId(Long studentId);

    @Query("SELECT AVG(p.progress) FROM Progress p WHERE p.student.id = :studentId")
    Double calculateAverageProgressForStudent(@Param("studentId") Long studentId);

    @Query("SELECT p FROM Progress p WHERE p.progress >= :minProgress AND p.student.id = :studentId")
    List<Progress> findProgressAboveThreshold(
        @Param("studentId") Long studentId, 
        @Param("minProgress") Double minProgress
    );

    @Query("SELECT COUNT(p) FROM Progress p WHERE p.student.id = :studentId AND p.progress >= 100")
    Long countCompletedCourses(@Param("studentId") Long studentId);
}