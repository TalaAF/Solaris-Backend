package com.example.lms.enrollment.assembler;

import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.mapper.EnrollmentMapper;
import com.example.lms.enrollment.model.Enrollment;
import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentAssembler {

    // Convert DTO to Entity
    public Enrollment toEntity(EnrollmentDTO dto, User student, Course course) {
        return EnrollmentMapper.toEntity(dto, student, course);
    }

    // Convert Entity to DTO
    public EnrollmentDTO toDTO(Enrollment enrollment) {
        return EnrollmentMapper.toDTO(enrollment);
    }
}
