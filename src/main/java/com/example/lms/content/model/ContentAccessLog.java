// ContentAccessLog.java (Entity)
package com.example.lms.content.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "content_access_log")
public class ContentAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long contentId;

    @CreationTimestamp
    private LocalDateTime accessedAt;
}