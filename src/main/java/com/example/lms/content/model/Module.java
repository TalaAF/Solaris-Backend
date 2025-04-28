package com.example.lms.content.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.lms.course.model.Course;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "module")
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Integer sequence;

    @Enumerated(EnumType.STRING)
    private ModuleStatus status = ModuleStatus.DRAFT;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Content> contents = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    // Release conditions
    private LocalDateTime releaseDate;
    private Boolean isReleased = false;
    
    /**
     * Check if the module is currently available to students
     * 
     * @return true if available, false otherwise
     */
    public boolean isAvailable() {
        if (ModuleStatus.PUBLISHED != status) {
            return false;
        }
        
        if (Boolean.FALSE.equals(isReleased)) {
            return false;
        }
        
        if (releaseDate != null && LocalDateTime.now().isBefore(releaseDate)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Add content to this module
     * 
     * @param content The content to add
     */
    public void addContent(Content content) {
        content.setModule(this);
        
        if (content.getOrder() == null) {
            // Set order to the end of the list
            content.setOrder(contents.size() + 1);
        }
        
        contents.add(content);
    }
}

/**
 * Enum representing the status of a module
 */
enum ModuleStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}