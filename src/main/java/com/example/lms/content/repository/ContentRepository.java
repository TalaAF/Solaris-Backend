package com.example.lms.content.repository;

import com.example.lms.content.model.Content;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByCourseId(Long courseId);

    @Query("SELECT c FROM Content c WHERE CONCAT(c.title, ' ', c.description) LIKE %:keyword%")
List<Content> searchByKeyword(@Param("keyword") String keyword);

List<Content> findByFileType(String fileType);
List<Content> findByFileSizeGreaterThan(Long fileSize);
}