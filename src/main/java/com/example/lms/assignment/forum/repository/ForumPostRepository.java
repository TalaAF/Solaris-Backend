package com.example.lms.assignment.forum.repository;

import com.example.lms.assignment.forum.model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByThreadId(Long threadId);
}