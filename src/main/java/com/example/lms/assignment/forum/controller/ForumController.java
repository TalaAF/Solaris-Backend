package com.example.lms.assignment.forum.controller;

import com.example.lms.assignment.forum.dto.ForumPostDTO;
import com.example.lms.assignment.forum.dto.ForumThreadDTO;
import com.example.lms.assignment.forum.mapper.ForumMapper;
import com.example.lms.assignment.forum.model.ForumPost;
import com.example.lms.assignment.forum.model.ForumThread;
import com.example.lms.assignment.forum.service.ForumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// Class-level annotations
@RestController
@RequestMapping("/api/forums")
@Tag(name = "Forum", description = "APIs for managing course discussion forums")
@SecurityRequirement(name = "bearerAuth")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private ForumMapper forumMapper;

    // Method-level annotations
    @PostMapping("/threads")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT', 'ADMIN')")
    @Operation(summary = "Create a new forum thread", description = "Creates a new discussion thread for a course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Thread created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ForumThreadDTO> createThread(@RequestBody ForumThreadDTO threadDTO) {
        ForumThread thread = forumMapper.toForumThread(threadDTO);
        ForumThread savedThread = forumService.createThread(thread);
        return ResponseEntity.ok(forumMapper.toForumThreadDTO(savedThread));
    }

    // Method-level annotations
    @PostMapping("/posts")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT','ADMIN')")
    @Operation(summary = "Create a new forum post", description = "Creates a new post in an existing thread")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ForumPostDTO> createPost(@RequestBody ForumPostDTO postDTO) {
        ForumPost post = forumMapper.toForumPost(postDTO);
        ForumPost savedPost = forumService.createPost(post);
        return ResponseEntity.ok(forumMapper.toForumPostDTO(savedPost));
    }

    // Method-level annotations
    @GetMapping("/{courseId}/threads")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT','ADMIN')")
    @Operation(summary = "Get threads for a course", description = "Retrieves all threads for a specific course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Threads retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<ForumThreadDTO>> getThreads(@PathVariable Long courseId) {
        List<ForumThread> threads = forumService.getThreadsByCourse(courseId);
        return ResponseEntity.ok(threads.stream()
                .map(forumMapper::toForumThreadDTO)
                .collect(Collectors.toList()));
    }

    // Method-level annotations
    @GetMapping("/threads/{threadId}/posts")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT','ADMIN')")
    @Operation(summary = "Get posts for a thread", description = "Retrieves all posts in a specific thread")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<ForumPostDTO>> getPosts(@PathVariable Long threadId) {
        List<ForumPost> posts = forumService.getPostsByThread(threadId);
        return ResponseEntity.ok(posts.stream()
                .map(forumMapper::toForumPostDTO)
                .collect(Collectors.toList()));
    }
}