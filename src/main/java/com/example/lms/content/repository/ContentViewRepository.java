package com.example.lms.content.repository;

import com.example.lms.content.model.ContentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentViewRepository extends JpaRepository<ContentView, Long> {
    Optional<ContentView> findByUserIdAndContentId(Long userId, Long contentId);
}