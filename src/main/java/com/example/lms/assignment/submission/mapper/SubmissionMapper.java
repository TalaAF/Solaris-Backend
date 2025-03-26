package com.example.lms.assignment.submission.mapper;

import com.example.lms.assignment.submission.dto.SubmissionDTO;
import com.example.lms.assignment.submission.model.Submission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {
    SubmissionDTO toSubmissionDTO(Submission submission);
    Submission toSubmission(SubmissionDTO submissionDTO);
}