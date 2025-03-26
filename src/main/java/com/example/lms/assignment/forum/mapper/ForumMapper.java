package com.example.lms.assignment.forum.mapper;

import com.example.lms.assignment.forum.dto.ForumPostDTO;
import com.example.lms.assignment.forum.dto.ForumThreadDTO;
import com.example.lms.assignment.forum.model.ForumPost;
import com.example.lms.assignment.forum.model.ForumThread;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ForumMapper {
    ForumThreadDTO toForumThreadDTO(ForumThread forumThread);
    ForumThread toForumThread(ForumThreadDTO forumThreadDTO);

    ForumPostDTO toForumPostDTO(ForumPost forumPost);
    ForumPost toForumPost(ForumPostDTO forumPostDTO);
}