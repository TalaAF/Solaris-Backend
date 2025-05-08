package com.example.lms.enrollment.dto;

import lombok.Data;

@Data
public class StatusUpdateRequest {
    private String status; // "active" or "inactive" to align with frontend
}