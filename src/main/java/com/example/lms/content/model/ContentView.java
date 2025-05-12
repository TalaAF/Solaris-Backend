package com.example.lms.content.model;

import com.example.lms.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_views")
@Data
@NoArgsConstructor
public class ContentView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "content_id")
    private Content content;
    
    @Column(name = "view_date")
    private LocalDateTime viewDate;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
}