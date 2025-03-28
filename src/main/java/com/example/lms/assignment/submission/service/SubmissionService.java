package com.example.lms.assignment.submission.service;

import com.example.lms.assignment.assessment.model.Assignment;
import com.example.lms.assignment.assessment.model.Score;
import com.example.lms.assignment.assessment.repository.AssignmentRepository;
import com.example.lms.assignment.assessment.repository.ScoreRepository;
import com.example.lms.assignment.submission.model.Submission;
import com.example.lms.assignment.submission.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;



@Service
public class SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    // Load upload directory from application.yml
    private final String uploadDir;

    // Load max file size from application.yml
    private final long maxFileSize;

    // Define allowed file extensions
    private static final List<String> ALLOWED_FILE_EXTENSIONS = Arrays.asList(
        ".pdf", ".doc", ".docx", ".ppt", ".pptx", ".jpg", ".jpeg", ".png"
    );

    // Define allowed MIME types for each extension
    private static final Map<String, List<String>> ALLOWED_MIME_TYPES = new HashMap<>();

    // Initialize allowed MIME types
    static {
        ALLOWED_MIME_TYPES.put(".pdf", Arrays.asList("application/pdf"));
        ALLOWED_MIME_TYPES.put(".doc", Arrays.asList("application/msword"));
        ALLOWED_MIME_TYPES.put(".docx", Arrays.asList("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        ALLOWED_MIME_TYPES.put(".ppt", Arrays.asList("application/vnd.ms-powerpoint"));
        ALLOWED_MIME_TYPES.put(".pptx", Arrays.asList("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        ALLOWED_MIME_TYPES.put(".jpg", Arrays.asList("image/jpeg"));
        ALLOWED_MIME_TYPES.put(".jpeg", Arrays.asList("image/jpeg"));
        ALLOWED_MIME_TYPES.put(".png", Arrays.asList("image/png"));
    }

    // Constructor to inject uploadDir and maxFileSize from application.yml
    @Autowired
    public SubmissionService(
        @Value("${file.upload-dir:C:/lms/uploads}") String uploadDir, // Fallback to C:/lms/uploads if not found
        @Value("${spring.servlet.multipart.max-file-size}") String maxFileSize
    ) {
        this.uploadDir = uploadDir.endsWith(File.separator) ? uploadDir : uploadDir + File.separator;
        logger.info("Initializing upload directory from configuration: {}", this.uploadDir);
    
        // Parse maxFileSize (e.g., "5MB" -> 5242880 bytes)
        this.maxFileSize = parseFileSize(maxFileSize);
        logger.info("Max file size set to: {} bytes", this.maxFileSize);
    
        // Ensure the upload directory exists and is writable
        File uploadDirFile = new File(this.uploadDir);
        logger.info("Checking upload directory: {}", uploadDirFile.getAbsolutePath());
        if (!uploadDirFile.exists()) {
            logger.info("Upload directory does not exist, attempting to create: {}", this.uploadDir);
            boolean created = uploadDirFile.mkdirs();
            if (!created) {
                logger.error("Failed to create upload directory: {}", this.uploadDir);
                throw new IllegalStateException("Failed to create upload directory: " + this.uploadDir);
            }
            logger.info("Upload directory created successfully: {}", this.uploadDir);
        }
        if (!uploadDirFile.isDirectory()) {
            logger.error("Upload path is not a directory: {}", this.uploadDir);
            throw new IllegalStateException("Upload path is not a directory: " + this.uploadDir);
        }
        if (!uploadDirFile.canWrite()) {
            logger.error("Upload directory is not writable: {}", this.uploadDir);
            throw new IllegalStateException("Upload directory is not writable: " + this.uploadDir);
        }
        if (!uploadDirFile.canRead()) {
            logger.error("Upload directory is not readable: {}", this.uploadDir);
            throw new IllegalStateException("Upload directory is not readable: " + this.uploadDir);
        }
        logger.info("Upload directory is ready: {}", this.uploadDir);
    }

    public Submission submitAssignment(Long assignmentId, Long studentId, MultipartFile file) throws IOException {
        logger.info("Submitting assignment for assignmentId: {}, studentId: {}", assignmentId, studentId);

        if (assignmentId == null || studentId == null) {
            logger.warn("Submission rejected: assignmentId or studentId is null (assignmentId: {}, studentId: {})", assignmentId, studentId);
            throw new IllegalArgumentException("Assignment ID and student ID are required");
        }

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + assignmentId));

        if (LocalDateTime.now().isAfter(assignment.getDueDate())) {
            logger.warn("Submission rejected: past due date for assignmentId: {}", assignmentId);
            throw new IllegalStateException("Submission is past due date");
        }

        if (file == null || file.isEmpty()) {
            logger.warn("Submission rejected: file is empty for assignmentId: {}", assignmentId);
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            logger.warn("Submission rejected: file size exceeds limit ({} bytes) for assignmentId: {}", file.getSize(), assignmentId);
            throw new IllegalArgumentException("File size exceeds the maximum limit of " + maxFileSize + " bytes");
        }

        String fileName = file.getOriginalFilename();
        String mimeType = file.getContentType();
        logger.info("Processing file: {}, MIME type: {}", fileName, mimeType);

        // Validate file name for security (prevent directory traversal)
        if (fileName == null || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            logger.warn("Submission rejected: invalid file name: {}", fileName);
            throw new IllegalArgumentException("Invalid file name");
        }

        // Normalize file name (replace special characters)
        String normalizedFileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        logger.debug("Normalized file name: {} -> {}", fileName, normalizedFileName);

        // Validate file type (extension and MIME type)
        if (!isAllowedFileType(normalizedFileName, mimeType)) {
            logger.warn("Submission rejected: invalid file type for file: {}, MIME type: {}", normalizedFileName, mimeType);
            throw new IllegalArgumentException("Only PDF, Word (.doc, .docx), PowerPoint (.ppt, .pptx), and image (.jpg, .jpeg, .png) files are allowed");
        }

        // Generate a unique file name to avoid conflicts
        String newFileName = studentId + "_" + System.currentTimeMillis() + "_" + normalizedFileName;
        String filePath = uploadDir + newFileName;
        File dest = new File(filePath);
        logger.info("Saving file to: {}", filePath);
        logger.info("Absolute path: {}", dest.getAbsolutePath());

        try {
            File parentDir = dest.getParentFile();
            if (!parentDir.exists()) {
                logger.info("Parent directory does not exist, creating: {}", parentDir.getAbsolutePath());
                boolean created = parentDir.mkdirs();
                if (!created) {
                    logger.error("Failed to create directory: {}", parentDir.getAbsolutePath());
                    throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
                }
                logger.info("Parent directory created: {}", parentDir.getAbsolutePath());
            }
            if (!parentDir.canWrite()) {
                logger.error("Parent directory is not writable: {}", parentDir.getAbsolutePath());
                throw new IOException("Parent directory is not writable: " + parentDir.getAbsolutePath());
            }
            file.transferTo(dest);
            logger.info("File saved successfully: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to save file to {}: {}", filePath, e.getMessage(), e);
            throw new IOException("Failed to save file to " + filePath + ": " + e.getMessage(), e);
        }

        Submission submission = new Submission();
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setFilePath(filePath);
        submission.setSubmissionDate(LocalDateTime.now());

        Submission savedSubmission = submissionRepository.save(submission);
        logger.info("Submission saved successfully: submissionId: {}", savedSubmission.getId());
        return savedSubmission;
    }

    public Submission getSubmission(Long submissionId) {
        logger.info("Fetching submission with id={}", submissionId);
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found with ID: " + submissionId));
        logger.info("Submission retrieved: id={}", submission.getId());
        return submission;
    }

    public Submission reviewSubmission(Long submissionId, String feedback, Integer grade) {
        logger.info("Reviewing submission: submissionId: {}, grade: {}, feedback: {}", submissionId, grade, feedback);

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found with ID: " + submissionId));

        if (grade != null && (grade < 0 || grade > 100)) {
            logger.warn("Review rejected: invalid grade: {} for submissionId: {}", grade, submissionId);
            throw new IllegalArgumentException("Grade must be between 0 and 100");
        }

        submission.setFeedback(feedback);
        submission.setGrade(grade);
        submission = submissionRepository.save(submission);
        logger.info("Submission updated: submissionId: {}", submissionId);

        if (grade != null) {
            Score score = new Score();
            score.setStudentId(submission.getStudentId());
            score.setAssignmentId(submission.getAssignmentId());
            score.setScore(grade);
            score.setGradedDate(LocalDateTime.now());
            scoreRepository.save(score);
            logger.info("Score saved for studentId: {}, assignmentId: {}, score: {}", submission.getStudentId(), submission.getAssignmentId(), grade);
        }

        return submission;
    }

    private boolean isAllowedFileType(String fileName, String mimeType) {
        if (fileName == null || mimeType == null) {
            logger.warn("File type validation failed: fileName or mimeType is null (fileName: {}, mimeType: {})", fileName, mimeType);
            return false;
        }
        String extension = fileName.toLowerCase();
        // Check if the extension is allowed
        boolean isExtensionAllowed = ALLOWED_FILE_EXTENSIONS.stream().anyMatch(extension::endsWith);
        if (!isExtensionAllowed) {
            logger.warn("File type validation failed: extension not allowed: {}", extension);
            return false;
        }
        // Check if the MIME type matches the extension
        List<String> allowedMimeTypes = ALLOWED_MIME_TYPES.getOrDefault(
            extension.substring(extension.lastIndexOf(".")), Collections.emptyList()
        );
        boolean isMimeTypeAllowed = allowedMimeTypes.contains(mimeType.toLowerCase());
        if (!isMimeTypeAllowed) {
            logger.warn("File type validation failed: MIME type {} does not match allowed types for extension {}: {}", mimeType, extension, allowedMimeTypes);
        }
        return isMimeTypeAllowed;
    }

    private long parseFileSize(String size) {
        if (size == null || size.isEmpty()) {
            throw new IllegalArgumentException("Max file size is not specified in configuration");
        }
        size = size.toUpperCase();
        long multiplier = 1;
        if (size.endsWith("KB")) {
            multiplier = 1024;
            size = size.replace("KB", "");
        } else if (size.endsWith("MB")) {
            multiplier = 1024 * 1024;
            size = size.replace("MB", "");
        } else if (size.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            size = size.replace("GB", "");
        }
        try {
            return Long.parseLong(size.trim()) * multiplier;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid max file size format: " + size, e);
        }
    }

}