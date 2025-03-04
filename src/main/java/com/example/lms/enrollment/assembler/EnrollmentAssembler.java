package com.example.lms.enrollment.assembler;

import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.mapper.EnrollmentMapper;
import com.example.lms.enrollment.model.Enrollment;
import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentAssembler {

    public Enrollment toEntity(EnrollmentDTO dto, User student, Course course) {
        Enrollment enrollment = EnrollmentMapper.toEntity(dto);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        return enrollment;
    }
}
