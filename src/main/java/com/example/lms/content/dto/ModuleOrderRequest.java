package com.example.lms.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for module reordering requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleOrderRequest {
    private Long moduleId;
    private Integer newSequence;
}