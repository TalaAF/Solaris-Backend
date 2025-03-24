package com.example.lms.assignment.forum.controller;

import com.example.lms.assignment.forum.dto.ForumPostDTO;
import com.example.lms.assignment.forum.dto.ForumThreadDTO;
import com.example.lms.assignment.forum.mapper.ForumMapper;
import com.example.lms.assignment.forum.model.ForumPost;
import com.example.lms.assignment.forum.model.ForumThread;
import com.example.lms.assignment.forum.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forums")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private ForumMapper forumMapper;

    @PostMapping("/threads")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ForumThreadDTO> createThread(@RequestBody ForumThreadDTO threadDTO) {
        ForumThread thread = forumMapper.toForumThread(threadDTO);
        ForumThread savedThread = forumService.createThread(thread);
        return ResponseEntity.ok(forumMapper.toForumThreadDTO(savedThread));
    }

    @PostMapping("/posts")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ForumPostDTO> createPost(@RequestBody ForumPostDTO postDTO) {
        ForumPost post = forumMapper.toForumPost(postDTO);
        ForumPost savedPost = forumService.createPost(post);
        return ResponseEntity.ok(forumMapper.toForumPostDTO(savedPost));
    }

    @GetMapping("/{courseId}/threads")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<ForumThreadDTO>> getThreads(@PathVariable Long courseId) {
        List<ForumThread> threads = forumService.getThreadsByCourse(courseId);
        return ResponseEntity.ok(threads.stream()
                .map(forumMapper::toForumThreadDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/threads/{threadId}/posts")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<ForumPostDTO>> getPosts(@PathVariable Long threadId) {
        List<ForumPost> posts = forumService.getPostsByThread(threadId);
        return ResponseEntity.ok(posts.stream()
                .map(forumMapper::toForumPostDTO)
                .collect(Collectors.toList()));
    }
}