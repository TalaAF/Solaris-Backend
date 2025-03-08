package com.example.lms.content.model;

import jakarta.persistence.*;
import lombok.Data;
import com.example.lms.course.model.Course;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "content")

public class Content {

    private Integer order;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String filePath;  // path of stored file

    @Column
    private String fileType;  // File type (eg PDF, MP4)


    @Column
    private Long fileSize;  // File size in bytes

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    // Relationship with the course
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Relationship with the module
    @ManyToOne
    @JoinColumn(name = "module_id")
     private Module module;

    // Relationship with the tags
    @ManyToMany
@JoinTable(
    name = "content_tag",
    joinColumns = @JoinColumn(name = "content_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id")
)
private List<Tag> tags;

}