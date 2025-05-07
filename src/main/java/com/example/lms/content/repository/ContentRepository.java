package com.example.lms.content.repository;

import com.example.lms.content.model.Content;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByCourseId(Long courseId);

    @Query("SELECT c FROM Content c WHERE CONCAT(c.title, ' ', c.description) LIKE %:keyword%")
    List<Content> searchByKeyword(@Param("keyword") String keyword);

    List<Content> findByFileType(String fileType);
    List<Content> findByFileSizeGreaterThan(Long fileSize);

    @Query("SELECT c FROM Content c JOIN c.tags t WHERE t.name IN :tags")
    List<Content> findByTags(@Param("tags") List<String> tags);

    Page<Content> findByTitleContainingOrDescriptionContaining(String title, String description, Pageable pageable);

    @Query("SELECT c FROM Content c JOIN c.tags t WHERE t.name IN :tags AND c.fileType = :fileType")
    List<Content> findByTagsAndFileType(@Param("tags") List<String> tags, @Param("fileType") String fileType);

    @Query("SELECT c FROM Content c WHERE " +
           "(:searchTerm IS NULL OR LOWER(c.title) LIKE :searchTerm OR LOWER(c.description) LIKE :searchTerm) AND " +
           "(:fileType IS NULL OR c.fileType = :fileType) AND " +
           "(:isPublished IS NULL OR c.isPublished = :isPublished) AND " +
           "(:tags IS NULL OR EXISTS (SELECT t FROM c.tags t WHERE t.name IN :tags))")
    Page<Content> findBySearchCriteria(@Param("searchTerm") String searchTerm,
                                       @Param("fileType") String fileType,
                                       @Param("tags") List<String> tags,
                                       @Param("isPublished") Boolean isPublished,
                                       Pageable pageable);

    @Query("SELECT c FROM Content c WHERE " +
           "(:fileType IS NULL OR c.fileType = :fileType) AND " +
           "(:isPublished IS NULL OR c.isPublished = :isPublished) AND " +
           "(:tags IS NULL OR EXISTS (SELECT t FROM c.tags t WHERE t.name IN :tags))")
    Page<Content> findByFilters(@Param("fileType") String fileType,
                                 @Param("tags") List<String> tags,
                                 @Param("isPublished") Boolean isPublished,
                                 Pageable pageable);
}