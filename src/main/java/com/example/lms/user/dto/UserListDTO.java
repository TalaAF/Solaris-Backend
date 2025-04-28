package com.example.lms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListDTO {
    private Long id;
    private String email;
    private String fullName;
    private boolean isActive;
    private Set<String> roleNames = new HashSet<>();
    private Long departmentId;
    private String departmentName;
    private LocalDateTime createdAt;
    
    /**
     * Gets the name as expected by frontend.
     * @return The user's full name
     */
    public String getName() {
        return this.fullName;
    }
}