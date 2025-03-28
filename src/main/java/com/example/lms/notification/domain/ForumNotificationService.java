package com.example.lms.notification.domain;

import com.example.lms.assignment.forum.model.ForumPost;
import com.example.lms.assignment.forum.repository.ForumPostRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.notification.service.NotificationService;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumNotificationService {
    
    private final NotificationService notificationService;
    private final ForumPostRepository forumPostRepository;
    private final UserRepository userRepository;
    
    /**
     * Notify users about replies to their forum posts
     */
    @Transactional
    @Async
    public void notifyForumReply(Long forumPostId, Long replyId, Long authorId) {
        try {
            ForumPost originalPost = forumPostRepository.findById(forumPostId)
                    .orElseThrow(() -> new ResourceNotFoundException("Forum post not found with id: " + forumPostId));
            
            ForumPost reply = forumPostRepository.findById(replyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Reply post not found with id: " + replyId));
            
            User replyAuthor = userRepository.findById(authorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + authorId));
            
            User originalAuthor = userRepository.findById(originalPost.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Original post author not found"));
            
            // Don't notify if the reply author is the same as the original post author
            if (originalAuthor.getId().equals(authorId)) {
                return;
            }
            
            String title = "New Reply to Your Forum Post";
            String content = replyAuthor.getFullName() + " replied to your post: " + 
                    (originalPost.getContent().length() > 50 ? 
                    originalPost.getContent().substring(0, 50) + "..." : 
                    originalPost.getContent());
            
            Map<String, Object> data = new HashMap<>();
            data.put("replyAuthorName", replyAuthor.getFullName());
            data.put("replyContent", reply.getContent());
            data.put("originalPostContent", originalPost.getContent());
            data.put("threadTitle", "Forum Discussion"); // Add thread title if available
            
            notificationService.createNotification(
                    NotificationType.FORUM_REPLY,
                    originalAuthor,
                    title,
                    content,
                    forumPostId,
                    "forumPost",
                    data
            );
            
            log.info("Sent forum reply notification to user ID: {} for post ID: {}", 
                    originalAuthor.getId(), forumPostId);
        } catch (Exception e) {
            log.error("Error sending forum reply notification for post ID: " + forumPostId, e);
        }
    }
    
    /**
     * Notify users when they are mentioned in forum posts
     */
    @Transactional
    @Async
    public void notifyForumMention(Long forumPostId, Long mentionedUserId, Long authorId) {
        try {
            ForumPost post = forumPostRepository.findById(forumPostId)
                    .orElseThrow(() -> new ResourceNotFoundException("Forum post not found with id: " + forumPostId));
            
            User mentionedUser = userRepository.findById(mentionedUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mentioned user not found with id: " + mentionedUserId));
            
            User author = userRepository.findById(authorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + authorId));
            
            // Don't notify if the author mentioned themselves
            if (mentionedUserId.equals(authorId)) {
                return;
            }
            
            String title = "You Were Mentioned in a Forum Post";
            String content = author.getFullName() + " mentioned you in a forum post.";
            
            Map<String, Object> data = new HashMap<>();
            data.put("authorName", author.getFullName());
            data.put("postContent", post.getContent());
            data.put("threadTitle", "Forum Discussion"); // Add thread title if available
            
            notificationService.createNotification(
                    NotificationType.FORUM_MENTION,
                    mentionedUser,
                    title,
                    content,
                    forumPostId,
                    "forumPost",
                    data
            );
            
            log.info("Sent forum mention notification to user ID: {} from author ID: {}", 
                    mentionedUserId, authorId);
        } catch (Exception e) {
            log.error("Error sending forum mention notification to user ID: " + mentionedUserId, e);
        }
    }
}