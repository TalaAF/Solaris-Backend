package com.example.lms.assignment.forum.service;

import com.example.lms.assignment.forum.model.ForumPost;
import com.example.lms.assignment.forum.model.ForumThread;
import com.example.lms.assignment.forum.repository.ForumPostRepository;
import com.example.lms.assignment.forum.repository.ForumThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForumService {

    @Autowired
    private ForumThreadRepository forumThreadRepository;

    @Autowired
    private ForumPostRepository forumPostRepository;

    public ForumThread createThread(ForumThread thread) {
        if (thread.getCourseId() == null || thread.getTitle() == null || thread.getCreatedBy() == null) {
            throw new IllegalArgumentException("Course ID, title, and creator must be provided");
        }
        if (thread.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Thread title cannot be empty");
        }

        thread.setCreatedDate(LocalDateTime.now());
        return forumThreadRepository.save(thread);
    }

    public ForumPost createPost(ForumPost post) {
        if (post.getThreadId() == null || post.getUserId() == null || post.getContent() == null) {
            throw new IllegalArgumentException("Thread ID, user ID, and content must be provided");
        }
        if (post.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }

        ForumThread thread = forumThreadRepository.findById(post.getThreadId())
                .orElseThrow(() -> new IllegalArgumentException("Thread not found with ID: " + post.getThreadId()));

        post.setPostedDate(LocalDateTime.now());
        return forumPostRepository.save(post);
    }

    public List<ForumThread> getThreadsByCourse(Long courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("Course ID must be provided");
        }
        return forumThreadRepository.findByCourseId(courseId);
    }

    public List<ForumPost> getPostsByThread(Long threadId) {
        if (threadId == null) {
            throw new IllegalArgumentException("Thread ID must be provided");
        }
        return forumPostRepository.findByThreadId(threadId);
    }
}