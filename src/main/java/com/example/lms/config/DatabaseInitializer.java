package com.example.lms.config;

import com.example.lms.Department.model.Department;
import com.example.lms.Department.repository.DepartmentRepository;
import com.example.lms.assessment.model.*;
import com.example.lms.assessment.repository.*;
import com.example.lms.assignment.assignments.model.Assignment;
import com.example.lms.assignment.assignments.model.Score;
import com.example.lms.assignment.assignments.repository.AssignmentRepository;
import com.example.lms.assignment.assignments.repository.ScoreRepository;
import com.example.lms.assignment.forum.model.ForumPost;
import com.example.lms.assignment.forum.model.ForumThread;
import com.example.lms.assignment.forum.repository.ForumPostRepository;
import com.example.lms.assignment.forum.repository.ForumThreadRepository;
import com.example.lms.assignment.submission.model.Submission;
import com.example.lms.assignment.submission.repository.SubmissionRepository;
import com.example.lms.certificate.model.Certificate;
import com.example.lms.certificate.repository.CertificateRepository;
import com.example.lms.content.model.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.lms.content.model.Module;
import com.example.lms.content.repository.*;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.enrollment.model.Enrollment;
import com.example.lms.enrollment.model.EnrollmentStatus;
import com.example.lms.enrollment.repository.EnrollmentRepository;
import com.example.lms.notification.model.Notification;
import com.example.lms.notification.model.NotificationPreference;
import com.example.lms.notification.model.NotificationType;
import com.example.lms.notification.repository.NotificationPreferenceRepository;
import com.example.lms.notification.repository.NotificationRepository;
import com.example.lms.progress.model.ContentProgress;
import com.example.lms.progress.model.Progress;
import com.example.lms.progress.repository.ContentProgressRepository;
import com.example.lms.progress.repository.ProgressRepository;
import com.example.lms.security.model.Permission;
import com.example.lms.security.model.Role;
import com.example.lms.security.repository.PermissionRepository;
import com.example.lms.security.repository.RoleRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.model.UserProfile;
import com.example.lms.user.repository.UserProfileRepository;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Healthcare LMS Database Initializer
 * Populates the database with realistic medical education dummy data
 * for testing and development purposes.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("!prod") // Only run in non-production environments
public class DatabaseInitializer {

    private final PasswordEncoder passwordEncoder;

    @Value("${app.database.initialize:true}")
    private boolean initializeDatabase;

   
    
    @Bean
    public CommandLineRunner initDatabase(
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            ContentRepository contentRepository,
            ModuleRepository moduleRepository,
            TagRepository tagRepository,
            ContentProgressRepository contentProgressRepository,
            ProgressRepository progressRepository
            // Comment out repositories you don't need right now
            /*
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            AnswerOptionRepository answerOptionRepository,
            QuizAttemptRepository quizAttemptRepository,
            StudentAnswerRepository studentAnswerRepository,
            AssignmentRepository assignmentRepository,
            ScoreRepository scoreRepository,
            SubmissionRepository submissionRepository,
            ForumThreadRepository forumThreadRepository,
            ForumPostRepository forumPostRepository,
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository notificationPreferenceRepository,
            CertificateRepository certificateRepository
            */
    ) {
        return args -> {
            if (!initializeDatabase) {
                log.info("Database initialization is disabled via configuration.");
                return;
            }

            try {
                log.info("Loading Healthcare LMS database with test data...");
                // Check if data already exists
                if (userRepository.count() > 0) {
                    log.info("Database already contains data, skipping initialization.");
                    return;
                }

                // Create departments with its own transaction
                Map<String, Department> departments = initDepartments(departmentRepository);
                
                // Create roles and permissions
                Map<String, Role> roles = initRolesAndPermissions(roleRepository, permissionRepository);
                
                // Create users
                Map<String, User> users = initUsers(userRepository, roles, departments);
                
                // Create user profiles
                initUserProfiles(userProfileRepository, users);
                
                // Create courses - using your standard method (not frontend aligned)
                Map<String, Course> courses = initCourses(courseRepository, users, departments, userRepository, roles);
                
                // Create modules and tags - using your standard methods
                Map<String, Module> modules = initModules(moduleRepository, courses);
                Map<String, Tag> tags = initTags(tagRepository);
                
                // Create content
                Map<String, Content> contents = initContent(contentRepository, courses, modules, tags);
                
                // Create enrollments
                Map<String, Enrollment> enrollments = initEnrollments(enrollmentRepository, users, courses);
                
                // Create progress data
                initProgress(progressRepository, users, courses);
                initContentProgress(contentProgressRepository, enrollments, contents);
                
                // Comment out everything you don't need for now
                /*
                // Create assessment items
                Map<String, Quiz> quizzes = initQuizzes(quizRepository, courses);
                Map<String, Question> questions = initQuestions(questionRepository, quizzes);
                initAnswerOptions(answerOptionRepository, questions);
                
                // Create quiz attempts
                Map<String, QuizAttempt> attempts = initQuizAttempts(quizAttemptRepository, quizzes, users);
                initStudentAnswers(studentAnswerRepository, attempts, questions, answerOptionRepository);
                
                // Create assignments
                Map<String, Assignment> assignments = initAssignments(assignmentRepository, courses);
                initScores(scoreRepository, users, assignments);
                initSubmissions(submissionRepository, assignments, users);
                
                // Create forum discussions
                Map<String, ForumThread> threads = initForumThreads(forumThreadRepository, courses, users);
                initForumPosts(forumPostRepository, threads, users);
                
                // Create notifications
                initNotifications(notificationRepository, users, courses, quizzes, assignments, threads);
                initNotificationPreferences(notificationPreferenceRepository, users);
                
                // Create certificates
                initCertificates(certificateRepository, users, courses);
                */
                
                log.info("Database initialized with healthcare LMS test data aligned with frontend!");
            } catch (Exception e) {
                log.error("Failed to initialize database with test data", e);
                e.printStackTrace();
                throw e;
            }
        };
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
private void initProgress(ProgressRepository progressRepository, Map<String, User> users, Map<String, Course> courses) {
    log.info("Creating progress data aligned with frontend...");
    
    try {
        // Check if both user and course exist before creating progress record
        User studentJohn = users.get("studentJohn");
        Course anatomyPhysiology = courses.get("anatomyPhysiology");
        
        if (studentJohn != null && anatomyPhysiology != null) {
            createProgress(progressRepository, studentJohn, anatomyPhysiology, 65.0, LocalDateTime.now().minusDays(2));
        } else {
            log.warn("Cannot create progress: student or course is missing");
        }
        
        User johnForBiochem = users.get("studentJohn");
        Course biochemistry = courses.get("biochemistry");
        
        if (johnForBiochem != null && biochemistry != null) {
            createProgress(progressRepository, johnForBiochem, biochemistry, 80.0, LocalDateTime.now().minusDays(3));
        } else {
            log.warn("Cannot create progress: student or biochemistry course is missing");
        }
        
        log.info("Successfully created progress records");
    } catch (Exception e) {
        log.error("Error creating progress data: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initContentProgress(ContentProgressRepository contentProgressRepository, Map<String, Enrollment> enrollments, Map<String, Content> contents) {
    log.info("Creating content progress data aligned with frontend...");
    
    try {
        // Check if both enrollment and content exist before creating progress records
        Enrollment johnAnatomy = enrollments.get("johnAnatomy");
        Content courseOverview = contents.get("courseOverview");
        
        if (johnAnatomy != null && courseOverview != null) {
            createContentProgress(contentProgressRepository, johnAnatomy, courseOverview, 100.0, LocalDateTime.now().minusDays(28));
        } else {
            log.warn("Cannot create content progress: enrollment or content is missing. Enrollment: {}, Content: {}", 
                johnAnatomy != null ? "exists" : "missing", 
                courseOverview != null ? "exists" : "missing");
        }
        
        Enrollment johnAnatomyForTerminology = enrollments.get("johnAnatomy");
        Content anatomicalTerminology = contents.get("anatomicalTerminology");
        
        if (johnAnatomyForTerminology != null && anatomicalTerminology != null) {
            createContentProgress(contentProgressRepository, johnAnatomyForTerminology, anatomicalTerminology, 100.0, LocalDateTime.now().minusDays(27));
        } else {
            log.warn("Cannot create content progress: enrollment or anatomicalTerminology content is missing");
        }
        
        Enrollment johnAnatomyForQuiz = enrollments.get("johnAnatomy");
        Content bodyPlanesQuiz = contents.get("bodyPlanesQuiz");
        
        if (johnAnatomyForQuiz != null && bodyPlanesQuiz != null) {
            createContentProgress(contentProgressRepository, johnAnatomyForQuiz, bodyPlanesQuiz, 100.0, LocalDateTime.now().minusDays(26));
        } else {
            log.warn("Cannot create content progress: enrollment or bodyPlanesQuiz content is missing");
        }
        
        log.info("Successfully created content progress records");
    } catch (Exception e) {
        log.error("Error creating content progress data: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Quiz> initQuizzes(QuizRepository quizRepository, Map<String, Course> courses) {
    log.info("Creating quizzes...");
    Map<String, Quiz> quizzes = new HashMap<>();
    
    try {
        // Updated course references to match what's in initCourses
        Quiz anatomyQuiz = createQuiz(quizRepository, "Human Anatomy Midterm", 
            "Comprehensive assessment of skeletal and muscular systems", 
            60, LocalDateTime.now().minusDays(40), LocalDateTime.now().minusDays(39), 
            70.0, true, true, courses.get("anatomyPhysiology")); // Changed from "anatomy" to "anatomyPhysiology"
        
        quizzes.put("anatomyQuiz", anatomyQuiz);
        
        log.info("Successfully created {} quizzes", quizzes.size());
    } catch (Exception e) {
        log.error("Error creating quizzes: " + e.getMessage(), e);
    }
    
    return quizzes;
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Question> initQuestions(QuestionRepository questionRepository, Map<String, Quiz> quizzes) {
    log.info("Creating quiz questions...");
    Map<String, Question> questions = new HashMap<>();
    
    try {
        // Create questions only for quizzes that exist
        if (quizzes.get("anatomyQuiz") != null) {
            Question q1 = createQuestion(questionRepository, "Which of the following is NOT a bone in the human wrist?", 
                QuestionType.MULTIPLE_CHOICE, 5, 1, "The human wrist contains 8 carpal bones.", quizzes.get("anatomyQuiz"));
            
            Question q2 = createQuestion(questionRepository, "The femur articulates with which of the following bones?", 
                QuestionType.MULTIPLE_ANSWER, 5, 2, "The femur articulates with the acetabulum of the pelvis and the tibia of the lower leg.", quizzes.get("anatomyQuiz"));
            
            Question q3 = createQuestion(questionRepository, "The biceps brachii muscle has its origin on the:", 
                QuestionType.MULTIPLE_CHOICE, 5, 3, "The biceps brachii has two heads with origins on the scapula.", quizzes.get("anatomyQuiz"));
            
            Question q4 = createQuestion(questionRepository, "The human heart has how many chambers?", 
                QuestionType.MULTIPLE_CHOICE, 5, 4, "The human heart is a four-chambered organ.", quizzes.get("anatomyQuiz"));
            
            Question q5 = createQuestion(questionRepository, "Describe the structure and function of the blood-brain barrier.", 
                QuestionType.ESSAY, 10, 5, "The blood-brain barrier is a highly selective semipermeable border that separates the circulating blood from the brain and extracellular fluid in the central nervous system.", quizzes.get("anatomyQuiz"));
            
            questions.put("q1", q1);
            questions.put("q2", q2);
            questions.put("q3", q3);
            questions.put("q4", q4);
            questions.put("q5", q5);
        }
        
        // Remove the cardiology quiz questions since that quiz doesn't exist
        // DO NOT create these questions that depend on non-existent quizzes
        /*
        Question cq1 = createQuestion(questionRepository, "The P wave on an ECG represents:", 
            QuestionType.MULTIPLE_CHOICE, 5, 1, "The P wave represents atrial depolarization.", quizzes.get("cardiologyQuiz"));
            
        Question cq2 = createQuestion(questionRepository, "Which of the following medications is NOT used to treat heart failure?", 
            QuestionType.MULTIPLE_CHOICE, 5, 2, "Common heart failure medications include ACE inhibitors, beta-blockers, diuretics, and ARBs.", quizzes.get("cardiologyQuiz"));
            
        questions.put("cq1", cq1);
        questions.put("cq2", cq2);
        */
        
        log.info("Successfully created {} questions", questions.size());
    } catch (Exception e) {
        log.error("Error creating questions: " + e.getMessage(), e);
    }
    
    return questions;
}

    @Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Enrollment> initEnrollments(EnrollmentRepository enrollmentRepository, Map<String, User> users, Map<String, Course> courses) {
    log.info("Creating enrollments aligned with frontend...");
    Map<String, Enrollment> enrollments = new HashMap<>();
    
    try {
        // Check if both the user and course exist before creating the enrollment
        User studentJohn = users.get("studentJohn");
        Course anatomyPhysiology = courses.get("anatomyPhysiology");
        
        if (studentJohn != null && anatomyPhysiology != null) {
            Enrollment johnAnatomy = createEnrollment(enrollmentRepository, studentJohn, anatomyPhysiology, 
                EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(30), 65.0);
            enrollments.put("johnAnatomy", johnAnatomy);
        } else {
            log.warn("Cannot create enrollment: student or course is missing. Student: {}, Course: {}", 
                studentJohn != null ? "exists" : "missing", 
                anatomyPhysiology != null ? "exists" : "missing");
        }
        
        User johnForBiochem = users.get("studentJohn");
        Course biochemistry = courses.get("biochemistry");
        
        if (johnForBiochem != null && biochemistry != null) {
            Enrollment johnBiochem = createEnrollment(enrollmentRepository, johnForBiochem, biochemistry, 
                EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(28), 80.0);
            enrollments.put("johnBiochem", johnBiochem);
        } else {
            log.warn("Cannot create enrollment: student or biochemistry course is missing. Student: {}, Biochemistry course: {}", 
                johnForBiochem != null ? "exists" : "missing", 
                biochemistry != null ? "exists" : "missing");
        }
        
        log.info("Successfully created {} enrollments", enrollments.size());
    } catch (Exception e) {
        log.error("Error creating enrollments: " + e.getMessage(), e);
    }
    
    return enrollments;
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Content> initContent(ContentRepository contentRepository, Map<String, Course> courses, Map<String, Module> modules, Map<String, Tag> tags) {
    log.info("Creating course content aligned with frontend...");
    Map<String, Content> contents = new HashMap<>();
    
    try {
        // Introduction to Anatomy Module content
        Content courseOverview = createContent(contentRepository, "Course Overview", 
            "Introduction to the course and its objectives", 
            courses.get("anatomyPhysiology"), ContentType.ARTICLE, "course_overview.pdf", "application/pdf", 512L, modules.get("introAnatomy"), 1);
        
        // Check if tags exist before adding them
        List<Tag> overviewTags = new ArrayList<>();
        if (tags.get("anatomy") != null) overviewTags.add(tags.get("anatomy"));
        if (tags.get("pdf") != null) overviewTags.add(tags.get("pdf"));
        courseOverview.setTags(overviewTags);
        contentRepository.save(courseOverview);
        contents.put("courseOverview", courseOverview);
        
        Content anatomicalTerminology = createContent(contentRepository, "Anatomical Terminology", 
            "Standard terminology used in anatomical descriptions", 
            courses.get("anatomyPhysiology"), ContentType.VIDEO, "anatomical_terminology.mp4", "video/mp4", 1536L, modules.get("introAnatomy"), 2);
        
        // Check if tags exist before adding them
        List<Tag> terminologyTags = new ArrayList<>();
        if (tags.get("anatomy") != null) terminologyTags.add(tags.get("anatomy"));
        if (tags.get("video") != null) terminologyTags.add(tags.get("video"));
        anatomicalTerminology.setTags(terminologyTags);
        contentRepository.save(anatomicalTerminology);
        contents.put("anatomicalTerminology", anatomicalTerminology);
        
        Content bodyPlanesQuiz = createContent(contentRepository, "Body Planes and Sections", 
            "Quiz on anatomical planes and body sections", 
            courses.get("anatomyPhysiology"), ContentType.QUIZ, "body_planes_quiz.json", "application/json", 256L, modules.get("introAnatomy"), 3);
        
        // Check if tags exist before adding them
        List<Tag> quizTags = new ArrayList<>();
        if (tags.get("anatomy") != null) quizTags.add(tags.get("anatomy"));
        if (tags.get("quiz") != null) quizTags.add(tags.get("quiz"));
        bodyPlanesQuiz.setTags(quizTags);
        contentRepository.save(bodyPlanesQuiz);
        contents.put("bodyPlanesQuiz", bodyPlanesQuiz);
        
        // Add more content items to match frontend data...
        
        log.info("Successfully created {} content items", contents.size());
    } catch (Exception e) {
        log.error("Error creating content: " + e.getMessage(), e);
    }
    
    return contents;
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, QuizAttempt> initQuizAttempts(QuizAttemptRepository quizAttemptRepository, Map<String, Quiz> quizzes, Map<String, User> users) {
    log.info("Creating quiz attempts...");
    Map<String, QuizAttempt> attempts = new HashMap<>();
    
    try {
        // Only create attempts if quiz and user exist
        if (quizzes.get("anatomyQuiz") != null && users.get("studentJohn") != null) {
            // Create a passing attempt
            QuizAttempt johnAnatomyAttempt = createQuizAttempt(
                quizAttemptRepository,
                quizzes.get("anatomyQuiz"),
                users.get("studentJohn"),
                LocalDateTime.now().minusDays(40),
                LocalDateTime.now().minusDays(40).plusMinutes(45),
                17.0, // Score out of 20
                85.0, // Percentage score
                true, // Passed
                AttemptStatus.COMPLETED
            );
            attempts.put("johnAnatomyAttempt", johnAnatomyAttempt);
        }
        
        // Add another attempt from a different student
        if (quizzes.get("anatomyQuiz") != null && users.get("studentJane") != null) {
            QuizAttempt janeAnatomyAttempt = createQuizAttempt(
                quizAttemptRepository,
                quizzes.get("anatomyQuiz"),
                users.get("studentJane"),
                LocalDateTime.now().minusDays(39),
                LocalDateTime.now().minusDays(39).plusMinutes(50),
                16.0, // Score out of 20
                80.0, // Percentage score
                true, // Passed
                AttemptStatus.COMPLETED
            );
            attempts.put("janeAnatomyAttempt", janeAnatomyAttempt);
        }
        
        log.info("Successfully created {} quiz attempts", attempts.size());
    } catch (Exception e) {
        log.error("Error creating quiz attempts: " + e.getMessage(), e);
    }
    
    return attempts;
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initStudentAnswers(StudentAnswerRepository studentAnswerRepository, Map<String, QuizAttempt> attempts, 
                             Map<String, Question> questions, AnswerOptionRepository answerOptionRepository) {
    log.info("Creating student answers...");
    
    try {
        // Only create answers if attempt and question exist
        if (attempts.get("johnAnatomyAttempt") != null && questions.get("q1") != null) {
            // Find the correct answer option for q1
            List<AnswerOption> q1Options = answerOptionRepository.findByQuestionId(questions.get("q1").getId());
            List<AnswerOption> selectedOptions = q1Options.stream()
                .filter(AnswerOption::isCorrect)
                .collect(Collectors.toList());
            
            // Create the student answer
            createStudentAnswer(
                studentAnswerRepository,
                attempts.get("johnAnatomyAttempt"),
                questions.get("q1"),
                selectedOptions,
                null, // No text answer for multiple choice
                5.0,  // Full points
                true, // Correct
                false, // Not manually graded
                null  // No instructor feedback
            );
            
            // Create more answers for other questions
            if (questions.get("q2") != null) {
                List<AnswerOption> q2Options = answerOptionRepository.findByQuestionId(questions.get("q2").getId());
                // In this case, let's assume the student got it partially correct
                List<AnswerOption> q2Selected = q2Options.stream()
                    .filter(o -> o.getOrderIndex() == 1 || o.getOrderIndex() == 3) // Selected options 1 and 3
                    .collect(Collectors.toList());
                
                createStudentAnswer(
                    studentAnswerRepository,
                    attempts.get("johnAnatomyAttempt"),
                    questions.get("q2"),
                    q2Selected,
                    null,
                    3.0, // Partial credit
                    false, // Not fully correct
                    false,
                    null
                );
            }
            
            // For an essay question
            if (questions.get("q5") != null) {
                createStudentAnswer(
                    studentAnswerRepository,
                    attempts.get("johnAnatomyAttempt"),
                    questions.get("q5"),
                    new ArrayList<>(), // No selected options for essay
                    "The blood-brain barrier is a selective membrane that separates the circulating blood from the brain and central nervous system. It consists of endothelial cells connected by tight junctions, which prevent many substances from entering the brain tissue. This protects the brain from harmful substances while allowing essential nutrients and oxygen to pass through.",
                    9.0, // 9 out of 10 points
                    false, // Not automatically correct
                    true, // Manually graded
                    "Good explanation of the structure and function, but could have mentioned more about the role of astrocytes in maintaining the barrier."
                );
            }
        }
        
        // Create answers for Jane's attempt also (if needed)
        
        log.info("Successfully created student answers");
    } catch (Exception e) {
        log.error("Error creating student answers: " + e.getMessage(), e);
    }
}

// Helper method to initialize submissions
@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initSubmissions(SubmissionRepository submissionRepository, Map<String, Assignment> assignments, Map<String, User> users) {
    log.info("Creating assignment submissions...");
    
    try {
        // Only create submissions for assignments and users that exist
        if (assignments.get("anatomyAssignment") != null && users.get("studentJohn") != null) {
            createSubmission(submissionRepository, 
                assignments.get("anatomyAssignment").getId(), 
                users.get("studentJohn").getId(), 
                "john_skeletal_system_assignment.pdf", 
                LocalDateTime.now().minusDays(48));
        }
        
        if (assignments.get("anatomyAssignment") != null && users.get("studentJane") != null) {
            createSubmission(submissionRepository, 
                assignments.get("anatomyAssignment").getId(), 
                users.get("studentJane").getId(), 
                "jane_skeletal_system_assignment.pdf", 
                LocalDateTime.now().minusDays(47));
        }
        
        log.info("Successfully created assignment submissions");
    } catch (Exception e) {
        log.error("Error creating submissions: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initAnswerOptions(AnswerOptionRepository answerOptionRepository, Map<String, Question> questions) {
    log.info("Creating answer options...");
    
    try {
        // Create answer options for question 1
        if (questions.get("q1") != null) {
            createAnswerOption(answerOptionRepository, "Trapezium", true, "Correct! The trapezium is one of the 8 carpal bones.", 1, questions.get("q1"));
            createAnswerOption(answerOptionRepository, "Phalanx", false, "Incorrect. Phalanges are found in the fingers and toes.", 2, questions.get("q1"));
            createAnswerOption(answerOptionRepository, "Metacarpal", false, "Incorrect. Metacarpals are found in the palm of the hand.", 3, questions.get("q1"));
            createAnswerOption(answerOptionRepository, "Patella", false, "Incorrect. The patella is the kneecap.", 4, questions.get("q1"));
        }
        
        // Create answer options for question 2
        if (questions.get("q2") != null) {
            createAnswerOption(answerOptionRepository, "Acetabulum", true, "Correct! The femur articulates with the acetabulum of the pelvis.", 1, questions.get("q2"));
            createAnswerOption(answerOptionRepository, "Humerus", false, "Incorrect. The humerus is in the upper arm.", 2, questions.get("q2"));
            createAnswerOption(answerOptionRepository, "Tibia", true, "Correct! The femur articulates with the tibia at the knee joint.", 3, questions.get("q2"));
            createAnswerOption(answerOptionRepository, "Fibula", false, "Incorrect. The femur does not directly articulate with the fibula.", 4, questions.get("q2"));
        }
        
        // Add more answer options for other questions
        
        log.info("Successfully created answer options");
    } catch (Exception e) {
        log.error("Error creating answer options: " + e.getMessage(), e);
    }
}
@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Module> initModules(ModuleRepository moduleRepository, Map<String, Course> courses) {
    log.info("Creating course modules aligned with frontend...");
    Map<String, Module> modules = new HashMap<>();
    
    try {
        // For Anatomy & Physiology
        Module introAnatomy = new Module();
        introAnatomy.setTitle("Introduction to Anatomy");
        introAnatomy.setDescription("Basic concepts and terminology in anatomy");
        introAnatomy.setSequence(1);
        introAnatomy.setStatus(ModuleStatus.PUBLISHED);
        introAnatomy.setIsReleased(true);
        introAnatomy.setCourse(courses.get("anatomyPhysiology"));
        moduleRepository.save(introAnatomy);
        modules.put("introAnatomy", introAnatomy);
        
        Module cellsTissues = new Module();
        cellsTissues.setTitle("Cells & Tissues");
        cellsTissues.setDescription("Cellular structure and tissue types");
        cellsTissues.setSequence(2);
        cellsTissues.setStatus(ModuleStatus.PUBLISHED);
        cellsTissues.setIsReleased(true);
        cellsTissues.setCourse(courses.get("anatomyPhysiology"));
        moduleRepository.save(cellsTissues);
        modules.put("cellsTissues", cellsTissues);
        
        Module skeletalSystem = new Module();
        skeletalSystem.setTitle("Skeletal System");
        skeletalSystem.setDescription("Bone structure, types, and skeletal anatomy");
        skeletalSystem.setSequence(3);
        skeletalSystem.setStatus(ModuleStatus.PUBLISHED);
        skeletalSystem.setIsReleased(false);
        skeletalSystem.setCourse(courses.get("anatomyPhysiology"));
        moduleRepository.save(skeletalSystem);
        modules.put("skeletalSystem", skeletalSystem);
        
        // Add more modules for other courses...
        
        log.info("Successfully created {} modules", modules.size());
    } catch (Exception e) {
        log.error("Error creating modules: " + e.getMessage(), e);
    }
    
    return modules;
}

    @Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Course> initCourses(CourseRepository courseRepository, Map<String, User> users, Map<String, Department> departments, UserRepository userRepository, Map<String, Role> roles) {
    log.info("Creating medical courses...");
    Map<String, Course> courses = new HashMap<>();
    
    try {
        // Create courses that match frontend data
        Course anatomyPhysiology = Course.builder()
                .title("Human Anatomy & Physiology")
                .description("A comprehensive study of human anatomy and physiology covering body systems, tissues, and organs.")
                .instructor(users.get("janeSmith"))
                .department(departments.get("anatomy"))
                .code("MED201")
                .maxCapacity(100)
                .semester("Fall 2025")
                .credits(4)
                .imageUrl("https://images.unsplash.com/photo-1532938911079-1b06ac7ceec7?q=80&w=1000&auto=format&fit=crop")
                .status("in-progress")
                .published(true)
                .archived(false)
                .startDate(LocalDateTime.now().minusMonths(1))
                .endDate(LocalDateTime.now().plusMonths(3))
                .students(new HashSet<>())
                .prerequisites(new HashSet<>())
                .build();
        
        // Add student enrollments to match frontend counts
        for (int i = 0; i < 68; i++) {
            User student = createDummyStudent(userRepository, "student" + i + "@medlms.com", 
                "Student " + i, "password123", roles.get("STUDENT"));
            anatomyPhysiology.getStudents().add(student);
        }
        
        courseRepository.save(anatomyPhysiology);
        courses.put("anatomyPhysiology", anatomyPhysiology);
        
        // Add biochemistry course that's referenced elsewhere
        Course biochemistry = Course.builder()
                .title("Biochemistry Fundamentals")
                .description("Study of chemical processes and substances in living organisms.")
                .instructor(users.get("robertJohnson"))
                .department(departments.get("biochemistry"))
                .code("MED202")
                .maxCapacity(80)
                .semester("Fall 2025")
                .credits(3)
                .imageUrl("https://images.unsplash.com/photo-1576086135878-bd1e45f50f00?q=80&w=1000&auto=format&fit=crop")
                .status("in-progress")
                .published(true)
                .archived(false)
                .startDate(LocalDateTime.now().minusMonths(1))
                .endDate(LocalDateTime.now().plusMonths(3))
                .students(new HashSet<>())
                .prerequisites(new HashSet<>())
                .build();
                
        courseRepository.save(biochemistry);
        courses.put("biochemistry", biochemistry);
        
        log.info("Successfully created {} courses aligned with frontend", courses.size());
    } catch (Exception e) {
        log.error("Error creating frontend-aligned courses: " + e.getMessage(), e);
    }
    
    return courses;
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Tag> initTags(TagRepository tagRepository) {
    log.info("Creating content tags...");
    Map<String, Tag> tags = new HashMap<>();
    
    try {
        Tag anatomyTag = createTag(tagRepository, "anatomy");
        Tag cardiologyTag = createTag(tagRepository, "cardiology");
        Tag radiologyTag = createTag(tagRepository, "radiology");
        Tag surgeryTag = createTag(tagRepository, "surgery");
        Tag neurologyTag = createTag(tagRepository, "neurology");
        Tag pediatricsTag = createTag(tagRepository, "pediatrics");
        Tag lectureTag = createTag(tagRepository, "lecture");
        Tag workshopTag = createTag(tagRepository, "workshop");
        Tag videoTag = createTag(tagRepository, "video");
        Tag pdfTag = createTag(tagRepository, "pdf");
        
        tags.put("anatomy", anatomyTag);
        tags.put("cardiology", cardiologyTag);
        tags.put("radiology", radiologyTag);
        tags.put("surgery", surgeryTag);
        tags.put("neurology", neurologyTag);
        tags.put("pediatrics", pediatricsTag);
        tags.put("lecture", lectureTag);
        tags.put("workshop", workshopTag);
        tags.put("video", videoTag);
        tags.put("pdf", pdfTag);
        
        log.info("Successfully created {} tags", tags.size());
    } catch (Exception e) {
        log.error("Error creating tags: " + e.getMessage(), e);
    }
    
    return tags;
}


    @Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Department> initDepartments(DepartmentRepository departmentRepository) {
    log.info("Creating medical departments...");
    Map<String, Department> departments = new HashMap<>();
    
    try {
        // Keep existing departments but align names with frontend data
        Department anatomy = createDepartment(departmentRepository, "Anatomy", "Study of body structure and organization", "ANAT", "Anatomical Sciences", "Dr. Jane Smith", "anatomy@example.com");
        Department biochemistry = createDepartment(departmentRepository, "Biochemistry", "Study of chemical processes and substances in living organisms", "BIOC", "Biochemical Sciences", "Dr. Robert Johnson", "biochemistry@example.com");
        Department pathology = createDepartment(departmentRepository, "Pathology", "Study of disease processes and their effects", "PATH", "Pathological Sciences", "Dr. Maria Garcia", "pathology@example.com");
        Department pharmacology = createDepartment(departmentRepository, "Pharmacology", "Study of drug action and effects", "PHARM", "Pharmaceutical Sciences", "Dr. David Chen", "pharmacology@example.com");
        Department medHumanities = createDepartment(departmentRepository, "Medical Humanities", "Study of ethics and humanities in medicine", "MH", "Medical Humanities", "Dr. Sarah Williams", "med-humanities@example.com");
        Department clinicalSciences = createDepartment(departmentRepository, "Clinical Sciences", "Study of applied clinical medicine", "CLIN", "Clinical Practice", "Dr. Michael Brown", "clinical-sciences@example.com");
        Department medFoundation = createDepartment(departmentRepository, "Medical Foundation", "Fundamental medical concepts and terminology", "MEDF", "Medical Foundation Studies", "Dr. Emily Taylor", "med-foundation@example.com");
        Department biology = createDepartment(departmentRepository, "Biology", "Study of living organisms and their processes", "BIO", "Biological Sciences", "Dr. Thomas White", "biology@example.com");

        departments.put("anatomy", anatomy);
        departments.put("biochemistry", biochemistry);
        departments.put("pathology", pathology);
        departments.put("pharmacology", pharmacology);
        departments.put("medHumanities", medHumanities);
        departments.put("clinicalSciences", clinicalSciences);
        departments.put("medFoundation", medFoundation);
        departments.put("biology", biology);
        
        log.info("Successfully created {} departments", departments.size());
    } catch (Exception e) {
        log.error("Error creating departments: " + e.getMessage(), e);
    }
    
    return departments;
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Role> initRolesAndPermissions(RoleRepository roleRepository, PermissionRepository permissionRepository) {
    log.info("Creating roles and permissions...");
    Map<String, Role> roles = new HashMap<>();
    
    try {
        roles = createRolesAndPermissions(roleRepository, permissionRepository);
        log.info("Successfully created roles and permissions");
    } catch (Exception e) {
        log.error("Error creating roles and permissions: " + e.getMessage(), e);
    }
    
    return roles;
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, User> initUsers(UserRepository userRepository, Map<String, Role> roles, Map<String, Department> departments) {
    log.info("Creating users...");
    Map<String, User> users = new HashMap<>();
    
    try {
        // Admin
        User adminUser = createUser(userRepository, "admin@medlms.com", "Admin User", "password123", roles.get("ADMIN"), null);
        users.put("admin", adminUser);
        
        // Instructors - matching frontend data
        User janeSmith = createUser(userRepository, "jsmith@medlms.com", "Dr. Jane Smith", "password123", roles.get("INSTRUCTOR"), departments.get("anatomy"));
        User robertJohnson = createUser(userRepository, "rjohnson@medlms.com", "Dr. Robert Johnson", "password123", roles.get("INSTRUCTOR"), departments.get("biochemistry"));
        User mariaGarcia = createUser(userRepository, "mgarcia@medlms.com", "Dr. Maria Garcia", "password123", roles.get("INSTRUCTOR"), departments.get("pathology"));
        User davidChen = createUser(userRepository, "dchen@medlms.com", "Dr. David Chen", "password123", roles.get("INSTRUCTOR"), departments.get("pharmacology"));
        User sarahWilliams = createUser(userRepository, "swilliams@medlms.com", "Dr. Sarah Williams", "password123", roles.get("INSTRUCTOR"), departments.get("medHumanities"));
        User michaelBrown = createUser(userRepository, "mbrown@medlms.com", "Dr. Michael Brown", "password123", roles.get("INSTRUCTOR"), departments.get("clinicalSciences"));
        User emilyTaylor = createUser(userRepository, "etaylor@medlms.com", "Dr. Emily Taylor", "password123", roles.get("INSTRUCTOR"), departments.get("medFoundation"));
        User thomasWhite = createUser(userRepository, "twhite@medlms.com", "Dr. Thomas White", "password123", roles.get("INSTRUCTOR"), departments.get("biology"));
        User lisaMartinez = createUser(userRepository, "lmartinez@medlms.com", "Dr. Lisa Martinez", "password123", roles.get("INSTRUCTOR"), departments.get("anatomy"));
        
        users.put("janeSmith", janeSmith);
        users.put("robertJohnson", robertJohnson);
        users.put("mariaGarcia", mariaGarcia);
        users.put("davidChen", davidChen);
        users.put("sarahWilliams", sarahWilliams);
        users.put("michaelBrown", michaelBrown);
        users.put("emilyTaylor", emilyTaylor);
        users.put("thomasWhite", thomasWhite);
        users.put("lisaMartinez", lisaMartinez);
        
        // Students - keeping existing students
        User studentJohn = createUser(userRepository, "jdoe@student.medlms.com", "John Doe", "password123", roles.get("STUDENT"), null);
        User studentJane = createUser(userRepository, "jalpha@student.medlms.com", "Jane Alpha", "password123", roles.get("STUDENT"), null);
        User studentSmith = createUser(userRepository, "smith@student.medlms.com", "John Smith", "password123", roles.get("STUDENT"), departments.get("anatomy"));
        User studentBrown = createUser(userRepository, "brown@student.medlms.com", "Maria Brown", "password123", roles.get("STUDENT"), departments.get("biochemistry"));
        
        users.put("studentJohn", studentJohn);
        users.put("studentJane", studentJane);
        users.put("smith", studentSmith);
        users.put("brown", studentBrown);
        
        log.info("Successfully created {} users", users.size());
    } catch (Exception e) {
        log.error("Error creating users: " + e.getMessage(), e);
    }
    
    return users;
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initUserProfiles(UserProfileRepository userProfileRepository, Map<String, User> users) {
    log.info("Creating user profiles...");
    
    try {
        createUserProfile(userProfileRepository, users.get("admin"), "Admin", "User", "123-456-7890", "System administrator with technical expertise.");
        
        // Updated user references to match what's in initUsers
        createUserProfile(userProfileRepository, users.get("janeSmith"), "Jane", "Smith", "123-555-1001", "Professor of Internal Medicine with 15 years of clinical experience. Research focus on cardiovascular health.");
        createUserProfile(userProfileRepository, users.get("davidChen"), "David", "Chen", "123-555-1002", "Chief of Surgery with specialization in minimally invasive procedures.");
        createUserProfile(userProfileRepository, users.get("mariaGarcia"), "Maria", "Garcia", "123-555-1003", "Pediatric specialist with expertise in developmental disorders.");
        createUserProfile(userProfileRepository, users.get("thomasWhite"), "Thomas", "White", "123-555-1004", "Director of Radiology with focus on advanced imaging techniques.");
        
        // Student profiles - using actual keys from initUsers
        createUserProfile(userProfileRepository, users.get("studentJohn"), "John", "Smith", "123-555-2001", "Third-year medical student interested in cardiology.");
        createUserProfile(userProfileRepository, users.get("studentJane"), "Jane", "Alpha", "123-555-2002", "Fourth-year medical student planning to specialize in orthopedic surgery.");
        createUserProfile(userProfileRepository, users.get("smith"), "John", "Smith", "123-555-2003", "Second-year medical student with interest in pediatric oncology.");
        createUserProfile(userProfileRepository, users.get("brown"), "Maria", "Brown", "123-555-2004", "Third-year medical student focusing on internal medicine and infectious diseases.");
        
        log.info("Successfully created user profiles");
    } catch (Exception e) {
        log.error("Error creating user profiles: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Assignment> initAssignments(AssignmentRepository assignmentRepository, Map<String, Course> courses) {
    log.info("Creating assignments...");
    Map<String, Assignment> assignments = new HashMap<>();
    
    try {
        // Check if course exists before accessing it
        if (courses.get("anatomyPhysiology") != null) {
            Assignment anatomyAssignment = createAssignment(assignmentRepository, courses.get("anatomyPhysiology").getId(), 
                "Skeletal System Diagram", 
                "Create a detailed diagram of the human skeletal system, labeling all major bones and joints. Include a brief description of the function of each bone group.", 
                LocalDateTime.now().minusDays(50), 100);
            assignments.put("anatomyAssignment", anatomyAssignment);
        }
        
        // Only add additional assignments for courses that exist
        // Add a biochemistry assignment since we know that course exists
        if (courses.get("biochemistry") != null) {
            Assignment biochemistryAssignment = createAssignment(assignmentRepository, courses.get("biochemistry").getId(),
                "Enzyme Activity Lab Report",
                "Complete a lab report on enzyme activity based on the experiments performed in lab session 3. Include methodology, results, and discussion sections.",
                LocalDateTime.now().minusDays(20), 100);
            assignments.put("biochemistryAssignment", biochemistryAssignment);
        }
        
        log.info("Successfully created {} assignments", assignments.size());
    } catch (Exception e) {
        log.error("Error creating assignments: " + e.getMessage(), e);
    }
    
    return assignments;
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initScores(ScoreRepository scoreRepository, Map<String, User> users, Map<String, Assignment> assignments) {
    log.info("Creating assignment scores...");
    
    try {
        // Only create scores for users and assignments that exist
        if (users.get("smith") != null && assignments.get("anatomyAssignment") != null) {
            createScore(scoreRepository, users.get("smith").getId(), assignments.get("anatomyAssignment").getId(), 92, LocalDateTime.now().minusDays(45));
        }
        
        if (users.get("brown") != null && assignments.get("anatomyAssignment") != null) {
            createScore(scoreRepository, users.get("brown").getId(), assignments.get("anatomyAssignment").getId(), 88, LocalDateTime.now().minusDays(44));
        }
        
        // Add more score entries with null checks
        if (users.get("studentJohn") != null && assignments.get("biochemistryAssignment") != null) {
            createScore(scoreRepository, users.get("studentJohn").getId(), assignments.get("biochemistryAssignment").getId(), 85, LocalDateTime.now().minusDays(10));
        }
        
        log.info("Successfully created assignment scores");
    } catch (Exception e) {
        log.error("Error creating assignment scores: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, ForumThread> initForumThreads(ForumThreadRepository forumThreadRepository, Map<String, Course> courses, Map<String, User> users) {
    log.info("Creating forum threads...");
    Map<String, ForumThread> threads = new HashMap<>();
    
    try {
        // Only create threads for courses that exist
        if (courses.get("anatomyPhysiology") != null && users.get("studentJohn") != null) {
            ForumThread anatomyThread = createForumThread(forumThreadRepository, courses.get("anatomyPhysiology").getId(), 
                "Difficulty understanding joint classifications", users.get("studentJohn").getId(), LocalDateTime.now().minusDays(52));
            threads.put("anatomyThread", anatomyThread);
        }
        
        if (courses.get("biochemistry") != null && users.get("studentJane") != null) {
            ForumThread biochemThread = createForumThread(forumThreadRepository, courses.get("biochemistry").getId(), 
                "Question about enzyme kinetics", users.get("studentJane").getId(), LocalDateTime.now().minusDays(25));
            threads.put("biochemThread", biochemThread);
        }
        
        log.info("Successfully created {} forum threads", threads.size());
    } catch (Exception e) {
        log.error("Error creating forum threads: " + e.getMessage(), e);
    }
    
    return threads;
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initForumPosts(ForumPostRepository forumPostRepository, Map<String, ForumThread> threads, Map<String, User> users) {
    log.info("Creating forum posts...");
    
    try {
        // Add null checks for all references
        if (threads.get("anatomyThread") != null && users.get("studentJohn") != null) {
            // Anatomy thread posts
            createForumPost(forumPostRepository, threads.get("anatomyThread").getId(), users.get("studentJohn").getId(), 
                "I'm having trouble understanding the classification of joints, particularly the difference between functional and structural classifications. Can someone explain this in simpler terms?", 
                LocalDateTime.now().minusDays(52));
        }
        
        if (threads.get("anatomyThread") != null && users.get("janeSmith") != null) {
            createForumPost(forumPostRepository, threads.get("anatomyThread").getId(), users.get("janeSmith").getId(), 
                "Great question! Structural classification refers to how the joints are physically connected (fibrous, cartilaginous, synovial), while functional classification refers to how much movement they allow (synarthrosis, amphiarthrosis, diarthrosis). For example, a suture in the skull is structurally fibrous and functionally a synarthrosis (immovable).", 
                LocalDateTime.now().minusDays(51));
        }
        
        // Add more forum posts with proper references
        
        log.info("Successfully created forum posts");
    } catch (Exception e) {
        log.error("Error creating forum posts: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initNotifications(NotificationRepository notificationRepository, Map<String, User> users, Map<String, Course> courses, 
                             Map<String, Quiz> quizzes, Map<String, Assignment> assignments, Map<String, ForumThread> threads) {
    log.info("Creating notifications...");
    
    try {
        // Only create notifications for entities that exist
        if (users.get("studentJohn") != null && courses.get("biochemistry") != null) {
            createNotification(notificationRepository, NotificationType.COURSE_CONTENT_UPLOAD, users.get("studentJohn"), 
                "New Content: Enzyme Kinetics", 
                "New content has been added to your course: Biochemistry Fundamentals", 
                courses.get("biochemistry").getId(), "course");
        }
        
        if (users.get("studentJohn") != null && assignments.get("anatomyAssignment") != null) {
            createNotification(notificationRepository, NotificationType.ASSIGNMENT_DEADLINE_24H, users.get("studentJohn"), 
                "Assignment Due in 24 Hours", 
                "Your assignment 'Skeletal System Diagram' is due soon.", 
                assignments.get("anatomyAssignment").getId(), "assignment");
        }
        
        // Add more notifications with null checks
        
        log.info("Successfully created notifications");
    } catch (Exception e) {
        log.error("Error creating notifications: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initNotificationPreferences(NotificationPreferenceRepository notificationPreferenceRepository, Map<String, User> users) {
    log.info("Creating notification preferences...");
    
    try {
        // Only create preferences for users that exist
        if (users.get("studentJohn") != null) {
            createNotificationPreference(notificationPreferenceRepository, users.get("studentJohn"), NotificationType.COURSE_CONTENT_UPLOAD, true, true);
            createNotificationPreference(notificationPreferenceRepository, users.get("studentJohn"), NotificationType.ASSIGNMENT_DEADLINE_24H, true, true);
            createNotificationPreference(notificationPreferenceRepository, users.get("studentJohn"), NotificationType.QUIZ_AVAILABLE, true, true);
            createNotificationPreference(notificationPreferenceRepository, users.get("studentJohn"), NotificationType.FORUM_REPLY, true, true);
            createNotificationPreference(notificationPreferenceRepository, users.get("studentJohn"), NotificationType.GRADE_POSTED, true, true);
        }
        
        if (users.get("studentJane") != null) {
            createNotificationPreference(notificationPreferenceRepository, users.get("studentJane"), NotificationType.COURSE_CONTENT_UPLOAD, true, false);
            createNotificationPreference(notificationPreferenceRepository, users.get("studentJane"), NotificationType.ASSIGNMENT_DEADLINE_24H, true, true);
        }
        
        // Remove references to non-existent users like "patel"
        
        log.info("Successfully created notification preferences");
    } catch (Exception e) {
        log.error("Error creating notification preferences: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initCertificates(CertificateRepository certificateRepository, Map<String, User> users, Map<String, Course> courses) {
    log.info("Creating certificates...");
    
    try {
        // Only create certificates for users and courses that exist
        if (users.get("studentJohn") != null && courses.get("anatomyPhysiology") != null) {
            createCertificate(certificateRepository, users.get("studentJohn").getId(), courses.get("anatomyPhysiology").getId(), 
                "certificate_john_anatomy.pdf", LocalDateTime.now().minusDays(14), "Human Anatomy & Physiology");
        }
        
        if (users.get("studentJane") != null && courses.get("biochemistry") != null) {
            createCertificate(certificateRepository, users.get("studentJane").getId(), courses.get("biochemistry").getId(), 
                "certificate_jane_biochem.pdf", LocalDateTime.now().minusDays(12), "Biochemistry Fundamentals");
        }
        
        log.info("Successfully created certificates");
    } catch (Exception e) {
        log.error("Error creating certificates: " + e.getMessage(), e);
    }
}
    
    // Helper methods to create entities
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Department createDepartment(DepartmentRepository repository, String name, String description, String code, String specialtyArea, String headOfDepartment, String contactInformation) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        department.setCode(code);
        department.setSpecialtyArea(specialtyArea);
        department.setHeadOfDepartment(headOfDepartment);
        department.setContactInformation(contactInformation);
        department.setActive(true);
        return repository.save(department);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Map<String, Role> createRolesAndPermissions(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        // Create permissions
        Permission userCreate = createPermission(permissionRepository, "user:create", "Can create users");
        Permission userRead = createPermission(permissionRepository, "user:read", "Can view user information");
        Permission userUpdate = createPermission(permissionRepository, "user:update", "Can update users");
        Permission userDelete = createPermission(permissionRepository, "user:delete", "Can delete users");
        
        Permission courseCreate = createPermission(permissionRepository, "course:create", "Can create courses");
        Permission courseRead = createPermission(permissionRepository, "course:read", "Can view course information");
        Permission courseUpdate = createPermission(permissionRepository, "course:update", "Can update courses");
        Permission courseDelete = createPermission(permissionRepository, "course:delete", "Can delete courses");
        
        Permission contentCreate = createPermission(permissionRepository, "content:create", "Can create content");
        Permission contentRead = createPermission(permissionRepository, "content:read", "Can view content");
        Permission contentUpdate = createPermission(permissionRepository, "content:update", "Can update content");
        Permission contentDelete = createPermission(permissionRepository, "content:delete", "Can delete content");
        
        Permission enrollmentCreate = createPermission(permissionRepository, "enrollment:create", "Can enroll in courses");
        Permission enrollmentRead = createPermission(permissionRepository, "enrollment:read", "Can view enrollments");
        Permission enrollmentUpdate = createPermission(permissionRepository, "enrollment:update", "Can update enrollments");
        Permission enrollmentDelete = createPermission(permissionRepository, "enrollment:delete", "Can delete enrollments");
        
        Permission adminAccess = createPermission(permissionRepository, "admin:access", "Access to admin functionality");
        
        // Create roles
        Role adminRole = createRole(roleRepository, "ADMIN", "Administrator with full access");
        Role instructorRole = createRole(roleRepository, "INSTRUCTOR", "Course instructor");
        Role studentRole = createRole(roleRepository, "STUDENT", "Enrolled student");
        
        // Assign permissions to roles
        // Admin role gets all permissions
        adminRole.setPermissions(Set.of(
            userCreate, userRead, userUpdate, userDelete,
            courseCreate, courseRead, courseUpdate, courseDelete,
            contentCreate, contentRead, contentUpdate, contentDelete,
            enrollmentCreate, enrollmentRead, enrollmentUpdate, enrollmentDelete,
            adminAccess
        ));
        
        // Instructor role gets course and content management permissions
        instructorRole.setPermissions(Set.of(
            userRead,
            courseCreate, courseRead, courseUpdate,
            contentCreate, contentRead, contentUpdate, contentDelete,
            enrollmentRead, enrollmentUpdate
        ));
        
        // Student role gets limited permissions
        studentRole.setPermissions(Set.of(
            userRead,
            courseRead,
            contentRead,
            enrollmentCreate, enrollmentRead, enrollmentDelete
        ));
        
        // Save roles with permissions
        roleRepository.save(adminRole);
        roleRepository.save(instructorRole);
        roleRepository.save(studentRole);
        
        // Return roles map for easy access
        Map<String, Role> roles = new HashMap<>();
        roles.put("ADMIN", adminRole);
        roles.put("INSTRUCTOR", instructorRole);
        roles.put("STUDENT", studentRole);
        
        return roles;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Permission createPermission(PermissionRepository repository, String name, String description) {
        return repository.findByName(name)
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(name);
                    permission.setDescription(description);
                    permission.setActive(true);
                    return repository.save(permission);
                });
    }
    
    private Role createRole(RoleRepository repository, String name, String description) {
        return repository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    role.setActive(true);
                    role.setPermissions(new HashSet<>());
                    return repository.save(role);
                });
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private User createUser(UserRepository repository, String email, String fullName, String password, Role role, Department department) {
        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .password(passwordEncoder.encode(password))
                .isActive(true)
                .tokenVersion(0L)
                .department(department)
                .build();
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        return repository.save(user);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private UserProfile createUserProfile(UserProfileRepository repository, User user, String firstName, String lastName, String phoneNumber, String biography) {
        UserProfile profile = UserProfile.builder()
                .user(user)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .biography(biography)
                .isProfileComplete(true)
                .build();
        return repository.save(profile);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Course createCourse(CourseRepository repository, String title, String description, User instructor, Department department, Integer maxCapacity) {
        Course course = Course.builder()
                .title(title)
                .description(description)
                .instructor(instructor)
                .department(department)
                .maxCapacity(maxCapacity)
                .students(new HashSet<>())
                .prerequisites(new HashSet<>())
                .build();
        return repository.save(course);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Module createModule(ModuleRepository repository, String title, String description, Integer sequence) {
        Module module = new Module();
        module.setTitle(title);
        module.setDescription(description);
        module.setSequence(sequence);
        return repository.save(module);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Tag createTag(TagRepository repository, String name) {
        Tag tag = new Tag();
        tag.setName(name);
        return repository.save(tag);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Content createContent(ContentRepository repository, String title, String description, Course course, ContentType type, String filePath, String fileType, Long fileSize, Module module, Integer order) {
        Content content = Content.builder()
                .title(title)
                .description(description)
                .course(course)
                .type(type)
                .filePath(filePath)
                .fileType(fileType)
                .fileSize(fileSize)
                .module(module)
                .order(order)
                .build();
        return repository.save(content);
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Enrollment createEnrollment(EnrollmentRepository repository, User student, Course course, EnrollmentStatus status, LocalDateTime enrollmentDate, Double progress) {
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(status)
                .enrollmentDate(enrollmentDate)
                .progress(progress)
                .build();
        return repository.save(enrollment);
    }
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    private Progress createProgress(ProgressRepository repository, User student, Course course, Double progress, LocalDateTime lastUpdated) {
        Progress progressEntity = new Progress(student, course, progress, lastUpdated);
        return repository.save(progressEntity);
    }
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    private ContentProgress createContentProgress(ContentProgressRepository repository, Enrollment enrollment, Content content, Double progress, LocalDateTime lastUpdated) {
        ContentProgress contentProgress = ContentProgress.builder()
                .enrollment(enrollment)
                .content(content)
                .progress(progress)
                .lastUpdated(lastUpdated)
                .build();
        return repository.save(contentProgress);
    }
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    private Quiz createQuiz(QuizRepository repository, String title, String description, Integer timeLimit, LocalDateTime startDate, LocalDateTime endDate, Double passingScore, boolean randomizeQuestions, boolean published, Course course) {
        Quiz quiz = Quiz.builder()
                .title(title)
                .description(description)
                .timeLimit(timeLimit)
                .startDate(startDate)
                .endDate(endDate)
                .passingScore(passingScore)
                .randomizeQuestions(randomizeQuestions)
                .published(published)
                .course(course)
                .build();
        return repository.save(quiz);
    }
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    private Question createQuestion(QuestionRepository repository, String text, QuestionType type, Integer points, Integer orderIndex, String feedback, Quiz quiz) {
        Question question = Question.builder()
                .text(text)
                .type(type)
                .points(points)
                .orderIndex(orderIndex)
                .feedback(feedback)
                .quiz(quiz)
                .build();
        return repository.save(question);
    }
    @Transactional  (propagation = Propagation.REQUIRES_NEW)
    private AnswerOption createAnswerOption(AnswerOptionRepository repository, String text, boolean isCorrect, String feedback, Integer orderIndex, Question question) {
        AnswerOption option = AnswerOption.builder()
                .text(text)
                .isCorrect(isCorrect)
                .feedback(feedback)
                .orderIndex(orderIndex)
                .question(question)
                .build();
        return repository.save(option);
    }
    @Transactional  (propagation = Propagation.REQUIRES_NEW)
    private QuizAttempt createQuizAttempt(QuizAttemptRepository repository, Quiz quiz, User student, LocalDateTime startedAt, LocalDateTime submittedAt, Double score, Double percentageScore, boolean passed, AttemptStatus status) {
        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .startedAt(startedAt)
                .submittedAt(submittedAt)
                .score(score)
                .percentageScore(percentageScore)
                .passed(passed)
                .status(status)
                .build();
        return repository.save(attempt);
    }
    
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    private StudentAnswer createStudentAnswer(StudentAnswerRepository repository, QuizAttempt attempt, Question question, List<AnswerOption> selectedOptions, String textAnswer, Double score, boolean isCorrect, boolean manuallyGraded, String instructorFeedback) {
        StudentAnswer answer = StudentAnswer.builder()
                .attempt(attempt)
                .question(question)
                .selectedOptions(selectedOptions)
                .textAnswer(textAnswer)
                .score(score)
                .isCorrect(isCorrect)
                .manuallyGraded(manuallyGraded)
                .instructorFeedback(instructorFeedback)
                .build();
        return repository.save(answer);
    }
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    private Assignment createAssignment(AssignmentRepository repository, Long courseId, String title, 
                                  String description, LocalDateTime dueDate, Integer maxScore) {
        Assignment assignment = new Assignment();
        assignment.setCourseId(courseId);
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setDueDate(dueDate);
        assignment.setMaxScore(maxScore);
        return repository.save(assignment);
    }
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    private Score createScore(ScoreRepository repository, Long studentId, Long assignmentId, Integer score, LocalDateTime gradedDate) {
        Score scoreEntity = new Score();
        scoreEntity.setStudentId(studentId);
        scoreEntity.setAssignmentId(assignmentId);
        scoreEntity.setScore(score);
        scoreEntity.setGradedDate(gradedDate);
        return repository.save(scoreEntity);
    }
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    private Submission createSubmission(SubmissionRepository repository, Long assignmentId, Long studentId, String filePath, LocalDateTime submissionDate) {
        Submission submission = new Submission();
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setFilePath(filePath);
        submission.setSubmissionDate(submissionDate);
        return repository.save(submission);
    }
    
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    private ForumThread createForumThread(ForumThreadRepository repository, Long courseId, String title, Long createdBy, LocalDateTime createdDate) {
        ForumThread thread = new ForumThread();
        thread.setCourseId(courseId);
        thread.setTitle(title);
        thread.setCreatedBy(createdBy);
        thread.setCreatedDate(createdDate);
        return repository.save(thread);
    }
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    private ForumPost createForumPost(ForumPostRepository repository, Long threadId, Long userId, String content, LocalDateTime postedDate) {
        ForumPost post = new ForumPost();
        post.setThreadId(threadId);
        post.setUserId(userId);
        post.setContent(content);
        post.setPostedDate(postedDate);
        return repository.save(post);
    }
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    private Notification createNotification(NotificationRepository repository, NotificationType type, User user, String title, String content, Long relatedEntityId, String relatedEntityType) {
        Notification notification = Notification.builder()
                .type(type)
                .user(user)
                .title(title)
                .content(content)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .read(false)
                .sent(true)
                .priority(getPriorityForNotificationType(type))
                .build();
        return repository.save(notification);
    }
    
    private int getPriorityForNotificationType(NotificationType type) {
        switch (type) {
            case ASSIGNMENT_DEADLINE_1H:
                return 5;
            case ASSIGNMENT_DEADLINE_12H:
                return 4;
            case ASSIGNMENT_DEADLINE_24H:
                return 3;
            case QUIZ_AVAILABLE:
            case GRADE_POSTED:
            case COURSE_ANNOUNCEMENT:
            case FORUM_MENTION:
                return 2;
            default:
                return 1;
        }
    }
    
    private NotificationPreference createNotificationPreference(NotificationPreferenceRepository repository, User user, NotificationType type, boolean emailEnabled, boolean inAppEnabled) {
        NotificationPreference preference = NotificationPreference.builder()
                .user(user)
                .type(type)
                .emailEnabled(emailEnabled)
                .inAppEnabled(inAppEnabled)
                .build();
        return repository.save(preference);
    }
    
    private Certificate createCertificate(CertificateRepository repository, Long studentId, Long courseId, String certificatePath, LocalDateTime issueDate, String courseName) {
        Certificate certificate = Certificate.builder()
                .studentId(studentId)
                .courseId(courseId)
                .certificateUrl(certificatePath)
                .issuedAt(issueDate)
                .courseName(courseName)
                .build();
        return repository.save(certificate);
    }

@Transactional(propagation = Propagation.REQUIRES_NEW)
private User createDummyStudent(UserRepository repository, String email, String fullName, 
                              String password, Role role) {
    User user = User.builder()
            .email(email)
            .fullName(fullName)
            .password(passwordEncoder.encode(password))
            .isActive(true)
            .tokenVersion(0L)
            .build();
    
    Set<Role> roles = new HashSet<>();
    roles.add(role);
    user.setRoles(roles);
    
    return repository.save(user);
}
}
