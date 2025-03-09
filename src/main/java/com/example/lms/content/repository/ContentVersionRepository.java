// ContentVersionRepository.java
package com.example.lms.content.repository;

import com.example.lms.content.model.ContentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentVersionRepository extends JpaRepository<ContentVersion, Long> {
    List<ContentVersion> findByContentId(Long contentId);
}