package com.example.lms.user.model;

import com.example.lms.common.BaseEntity;
import com.example.lms.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;


@Entity
@Table(name = "user_profiles")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(length = 50)
    private String firstName;
    
    @Column(length = 50)
    private String lastName;
    
    @Column(length = 15)
    private String phoneNumber;
    
    @Column(length = 500)
    private String biography;
    
    @Column(length = 255)
    private String profilePictureUrl;
    
    // Profile completion tracking
    private boolean isProfileComplete;
    
    @PrePersist
    @PreUpdate
    private void calculateProfileCompletion() {
        this.isProfileComplete = firstName != null && !firstName.isBlank() &&
                lastName != null && !lastName.isBlank() && 
                phoneNumber != null && !phoneNumber.isBlank();
    }
}