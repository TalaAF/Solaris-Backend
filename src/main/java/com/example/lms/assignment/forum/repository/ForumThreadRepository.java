package com.example.lms.assignment.forum.repository;

import com.example.lms.assignment.forum.model.ForumThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {
    List<ForumThread> findByCourseId(Long courseId);
}