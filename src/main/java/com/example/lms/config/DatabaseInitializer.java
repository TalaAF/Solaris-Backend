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
            ProgressRepository progressRepository,
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
                
                // Create courses
                Map<String, Course> courses = initCourses(courseRepository, users, departments);
                
                // Create modules and tags
                Map<String, Module> modules = initModules(moduleRepository);
                Map<String, Tag> tags = initTags(tagRepository);
                
                // Create content
                Map<String, Content> contents = initContent(contentRepository, courses, modules, tags);
                
                // Create enrollments
                Map<String, Enrollment> enrollments = initEnrollments(enrollmentRepository, users, courses);
                
                // Create progress data
                initProgress(progressRepository, users, courses);
                initContentProgress(contentProgressRepository, enrollments, contents);
                
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
                
                log.info("Database initialized with healthcare LMS test data!");
            } catch (Exception e) {
                log.error("Failed to initialize database with test data", e);
                e.printStackTrace();
                throw e;
            }
        };
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
private void initProgress(ProgressRepository progressRepository, Map<String, User> users, Map<String, Course> courses) {
    log.info("Creating progress data...");
    
    try {
        // Track overall course progress
        createProgress(progressRepository, users.get("smith"), courses.get("anatomy"), 100.0, LocalDateTime.now().minusDays(15));
        createProgress(progressRepository, users.get("smith"), courses.get("cardiology"), 65.0, LocalDateTime.now().minusDays(2));
        createProgress(progressRepository, users.get("brown"), courses.get("anatomy"), 100.0, LocalDateTime.now().minusDays(16));
        createProgress(progressRepository, users.get("brown"), courses.get("surgicalTech"), 78.0, LocalDateTime.now().minusDays(3));
        createProgress(progressRepository, users.get("patel"), courses.get("anatomy"), 100.0, LocalDateTime.now().minusDays(14));
        createProgress(progressRepository, users.get("patel"), courses.get("cardiology"), 80.0, LocalDateTime.now().minusDays(4));
        
        log.info("Successfully created progress records");
    } catch (Exception e) {
        log.error("Error creating progress data: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initContentProgress(ContentProgressRepository contentProgressRepository, Map<String, Enrollment> enrollments, Map<String, Content> contents) {
    log.info("Creating content progress data...");
    
    try {
        // Track content-specific progress for Smith in Anatomy
        createContentProgress(contentProgressRepository, enrollments.get("smithAnatomy"), contents.get("skeletalLecture"), 100.0, LocalDateTime.now().minusDays(55));
        createContentProgress(contentProgressRepository, enrollments.get("smithAnatomy"), contents.get("jointLecture"), 100.0, LocalDateTime.now().minusDays(50));
        createContentProgress(contentProgressRepository, enrollments.get("smithAnatomy"), contents.get("boneDevelopment"), 100.0, LocalDateTime.now().minusDays(45));
        createContentProgress(contentProgressRepository, enrollments.get("smithAnatomy"), contents.get("muscleTypes"), 100.0, LocalDateTime.now().minusDays(40));
        createContentProgress(contentProgressRepository, enrollments.get("smithAnatomy"), contents.get("muscleContraction"), 100.0, LocalDateTime.now().minusDays(35));
        createContentProgress(contentProgressRepository, enrollments.get("smithAnatomy"), contents.get("heartAnatomy"), 100.0, LocalDateTime.now().minusDays(30));
        createContentProgress(contentProgressRepository, enrollments.get("smithAnatomy"), contents.get("vesselStructure"), 100.0, LocalDateTime.now().minusDays(25));
        createContentProgress(contentProgressRepository, enrollments.get("smithAnatomy"), contents.get("brainAnatomy"), 100.0, LocalDateTime.now().minusDays(20));
        createContentProgress(contentProgressRepository, enrollments.get("smithAnatomy"), contents.get("spinalCord"), 100.0, LocalDateTime.now().minusDays(15));
        
        // Create content progress for Smith in Cardiology
        createContentProgress(contentProgressRepository, enrollments.get("smithCardiology"), contents.get("ecgBasics"), 100.0, LocalDateTime.now().minusDays(10));
        createContentProgress(contentProgressRepository, enrollments.get("smithCardiology"), contents.get("heartFailure"), 30.0, LocalDateTime.now().minusDays(2));
        
        // Create content progress for Brown
        createContentProgress(contentProgressRepository, enrollments.get("brownAnatomy"), contents.get("skeletalLecture"), 100.0, LocalDateTime.now().minusDays(53));
        createContentProgress(contentProgressRepository, enrollments.get("brownAnatomy"), contents.get("jointLecture"), 100.0, LocalDateTime.now().minusDays(48));
        createContentProgress(contentProgressRepository, enrollments.get("brownAnatomy"), contents.get("boneDevelopment"), 100.0, LocalDateTime.now().minusDays(43));
        createContentProgress(contentProgressRepository, enrollments.get("brownAnatomy"), contents.get("muscleTypes"), 100.0, LocalDateTime.now().minusDays(38));
        createContentProgress(contentProgressRepository, enrollments.get("brownAnatomy"), contents.get("muscleContraction"), 100.0, LocalDateTime.now().minusDays(33));
        createContentProgress(contentProgressRepository, enrollments.get("brownAnatomy"), contents.get("heartAnatomy"), 100.0, LocalDateTime.now().minusDays(28));
        createContentProgress(contentProgressRepository, enrollments.get("brownAnatomy"), contents.get("vesselStructure"), 100.0, LocalDateTime.now().minusDays(23));
        createContentProgress(contentProgressRepository, enrollments.get("brownAnatomy"), contents.get("brainAnatomy"), 100.0, LocalDateTime.now().minusDays(18));
        createContentProgress(contentProgressRepository, enrollments.get("brownAnatomy"), contents.get("spinalCord"), 100.0, LocalDateTime.now().minusDays(13));
        
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
        // Anatomy quiz
        Quiz anatomyQuiz = createQuiz(quizRepository, "Human Anatomy Midterm", 
            "Comprehensive assessment of skeletal and muscular systems", 
            60, LocalDateTime.now().minusDays(40), LocalDateTime.now().minusDays(39), 
            70.0, true, true, courses.get("anatomy"));
        
        // Cardiology quiz
        Quiz cardiologyQuiz = createQuiz(quizRepository, "Cardiac Assessment", 
            "Evaluation of heart function and cardiac disorders", 
            45, LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(18), 
            75.0, true, true, courses.get("cardiology"));
        
        quizzes.put("anatomyQuiz", anatomyQuiz);
        quizzes.put("cardiologyQuiz", cardiologyQuiz);
        
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
        // Create questions for anatomy quiz
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
        
        // Create questions for cardiology quiz
        Question cq1 = createQuestion(questionRepository, "The P wave on an ECG represents:", 
            QuestionType.MULTIPLE_CHOICE, 5, 1, "The P wave represents atrial depolarization.", quizzes.get("cardiologyQuiz"));
        
        Question cq2 = createQuestion(questionRepository, "Which of the following medications is NOT used to treat heart failure?", 
            QuestionType.MULTIPLE_CHOICE, 5, 2, "Common heart failure medications include ACE inhibitors, beta-blockers, diuretics, and ARBs.", quizzes.get("cardiologyQuiz"));
        
        questions.put("q1", q1);
        questions.put("q2", q2);
        questions.put("q3", q3);
        questions.put("q4", q4);
        questions.put("q5", q5);
        questions.put("cq1", cq1);
        questions.put("cq2", cq2);
        
        log.info("Successfully created {} questions", questions.size());
    } catch (Exception e) {
        log.error("Error creating questions: " + e.getMessage(), e);
    }
    
    return questions;
}

    @Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Enrollment> initEnrollments(EnrollmentRepository enrollmentRepository, Map<String, User> users, Map<String, Course> courses) {
    log.info("Creating course enrollments...");
    Map<String, Enrollment> enrollments = new HashMap<>();
    
    try {
        // Enroll students in Anatomy (most basic course)
        Enrollment smithAnatomyEnrollment = createEnrollment(enrollmentRepository, users.get("smith"), courses.get("anatomy"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(60), 75.0);
        
        Enrollment brownAnatomyEnrollment = createEnrollment(enrollmentRepository, users.get("brown"), courses.get("anatomy"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(58), 85.0);
        
        Enrollment patelAnatomyEnrollment = createEnrollment(enrollmentRepository, users.get("patel"), courses.get("anatomy"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(62), 90.0);
        
        Enrollment garciaAnatomyEnrollment = createEnrollment(enrollmentRepository, users.get("garcia"), courses.get("anatomy"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(59), 70.0);
        
        Enrollment kimAnatomyEnrollment = createEnrollment(enrollmentRepository, users.get("kim"), courses.get("anatomy"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(57), 95.0);
        
        // Enroll students in Cardiology (after completing anatomy)
        Enrollment smithCardiologyEnrollment = createEnrollment(enrollmentRepository, users.get("smith"), courses.get("cardiology"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(30), 65.0);
        
        Enrollment patelCardiologyEnrollment = createEnrollment(enrollmentRepository, users.get("patel"), courses.get("cardiology"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(32), 80.0);
        
        Enrollment garciaCardiologyEnrollment = createEnrollment(enrollmentRepository, users.get("garcia"), courses.get("cardiology"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(28), 60.0);
        
        // Enroll students in Surgical Techniques
        Enrollment brownSurgicalEnrollment = createEnrollment(enrollmentRepository, users.get("brown"), courses.get("surgicalTech"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(25), 78.0);
        
        Enrollment kimSurgicalEnrollment = createEnrollment(enrollmentRepository, users.get("kim"), courses.get("surgicalTech"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(22), 92.0);
        
        // Enroll students in Pediatric Care
        Enrollment millerPediatricEnrollment = createEnrollment(enrollmentRepository, users.get("miller"), courses.get("pediatricCare"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(40), 85.0);
        
        Enrollment wilsonPediatricEnrollment = createEnrollment(enrollmentRepository, users.get("wilson"), courses.get("pediatricCare"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(39), 75.0);
        
        // Additional enrollments
        Enrollment davisOrthoEnrollment = createEnrollment(enrollmentRepository, users.get("davis"), courses.get("orthopedicSurgery"), 
            EnrollmentStatus.PENDING, LocalDateTime.now().minusDays(5), 0.0);
        
        Enrollment jonesNeuroEnrollment = createEnrollment(enrollmentRepository, users.get("jones"), courses.get("neurologicalDisorders"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(45), 82.0);
        
        Enrollment leeRadiologyEnrollment = createEnrollment(enrollmentRepository, users.get("lee"), courses.get("diagnosticImaging"), 
            EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(50), 88.0);

        enrollments.put("smithAnatomy", smithAnatomyEnrollment);
        enrollments.put("brownAnatomy", brownAnatomyEnrollment);
        enrollments.put("patelAnatomy", patelAnatomyEnrollment);
        enrollments.put("garciaAnatomy", garciaAnatomyEnrollment);
        enrollments.put("kimAnatomy", kimAnatomyEnrollment);
        enrollments.put("smithCardiology", smithCardiologyEnrollment);
        enrollments.put("patelCardiology", patelCardiologyEnrollment);
        enrollments.put("garciaCardiology", garciaCardiologyEnrollment);
        enrollments.put("brownSurgical", brownSurgicalEnrollment);
        enrollments.put("kimSurgical", kimSurgicalEnrollment);
        enrollments.put("millerPediatric", millerPediatricEnrollment);
        enrollments.put("wilsonPediatric", wilsonPediatricEnrollment);
        enrollments.put("davisOrtho", davisOrthoEnrollment);
        enrollments.put("jonesNeuro", jonesNeuroEnrollment);
        enrollments.put("leeRadiology", leeRadiologyEnrollment);
        
        log.info("Successfully created {} enrollments", enrollments.size());
    } catch (Exception e) {
        log.error("Error creating enrollments: " + e.getMessage(), e);
    }
    
    return enrollments;
}
    @Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Content> initContent(ContentRepository contentRepository, Map<String, Course> courses, Map<String, Module> modules, Map<String, Tag> tags) {
    log.info("Creating course content...");
    Map<String, Content> contents = new HashMap<>();
    
    try {
        // Create content for Anatomy modules
        Content skeletalLecture = createContent(contentRepository, "Skeletal System Overview", 
            "Comprehensive introduction to the human skeletal system", 
            courses.get("anatomy"), ContentType.VIDEO, "skeletal_system.mp4", "video/mp4", 1024L, modules.get("skeletal"), 1);
        skeletalLecture.setTags(List.of(tags.get("anatomy"), tags.get("lecture"), tags.get("video")));
        contentRepository.save(skeletalLecture);
        
        Content jointLecture = createContent(contentRepository, "Joint Structure and Function", 
            "Detailed examination of synovial, fibrous and cartilaginous joints", 
            courses.get("anatomy"), ContentType.VIDEO, "joints.mp4", "video/mp4", 1536L, modules.get("skeletal"), 2);
        jointLecture.setTags(List.of(tags.get("anatomy"), tags.get("lecture"), tags.get("video")));
        contentRepository.save(jointLecture);
        
        Content boneDevelopment = createContent(contentRepository, "Bone Development", 
            "Ossification process and bone growth through lifespan", 
            courses.get("anatomy"), ContentType.ARTICLE, "bone_development.pdf", "application/pdf", 2048L, modules.get("skeletal"), 3);
        boneDevelopment.setTags(List.of(tags.get("anatomy"), tags.get("pdf")));
        contentRepository.save(boneDevelopment);
        
        Content muscleTypes = createContent(contentRepository, "Types of Muscle Tissue", 
            "Skeletal, cardiac and smooth muscle structure and function", 
            courses.get("anatomy"), ContentType.VIDEO, "muscle_types.mp4", "video/mp4", 1280L, modules.get("muscular"), 1);
        muscleTypes.setTags(List.of(tags.get("anatomy"), tags.get("lecture"), tags.get("video")));
        contentRepository.save(muscleTypes);
        
        Content muscleContraction = createContent(contentRepository, "Muscle Contraction", 
            "Sliding filament theory and excitation-contraction coupling", 
            courses.get("anatomy"), ContentType.ARTICLE, "muscle_contraction.pdf", "application/pdf", 1792L, modules.get("muscular"), 2);
        muscleContraction.setTags(List.of(tags.get("anatomy"), tags.get("pdf")));
        contentRepository.save(muscleContraction);
        
        Content heartAnatomy = createContent(contentRepository, "Heart Anatomy", 
            "Chambers, valves and cardiac circulation", 
            courses.get("anatomy"), ContentType.VIDEO, "heart_anatomy.mp4", "video/mp4", 2304L, modules.get("cardiovascular"), 1);
        heartAnatomy.setTags(List.of(tags.get("anatomy"), tags.get("cardiology"), tags.get("video")));
        contentRepository.save(heartAnatomy);
        
        Content vesselStructure = createContent(contentRepository, "Blood Vessel Structure", 
            "Arteries, veins and capillaries - wall structure and specializations", 
            courses.get("anatomy"), ContentType.ARTICLE, "vessel_structure.pdf", "application/pdf", 1536L, modules.get("cardiovascular"), 2);
        vesselStructure.setTags(List.of(tags.get("anatomy"), tags.get("cardiology"), tags.get("pdf")));
        contentRepository.save(vesselStructure);
        
        Content brainAnatomy = createContent(contentRepository, "Brain Anatomy", 
            "Cerebrum, cerebellum, brain stem and functional areas", 
            courses.get("anatomy"), ContentType.VIDEO, "brain_anatomy.mp4", "video/mp4", 2560L, modules.get("nervous"), 1);
        brainAnatomy.setTags(List.of(tags.get("anatomy"), tags.get("neurology"), tags.get("video")));
        contentRepository.save(brainAnatomy);
        
        Content spinalCord = createContent(contentRepository, "Spinal Cord and Nerves", 
            "Spinal tracts, reflexes and peripheral nervous system", 
            courses.get("anatomy"), ContentType.ARTICLE, "spinal_cord.pdf", "application/pdf", 1792L, modules.get("nervous"), 2);
        spinalCord.setTags(List.of(tags.get("anatomy"), tags.get("neurology"), tags.get("pdf")));
        contentRepository.save(spinalCord);
        
        // Create content for other courses
        // Cardiology course content
        Content ecgBasics = createContent(contentRepository, "ECG Interpretation Basics", 
            "Fundamentals of electrocardiogram reading and analysis", 
            courses.get("cardiology"), ContentType.VIDEO, "ecg_basics.mp4", "video/mp4", 1848L, modules.get("heartPhysiology"), 1);
        ecgBasics.setTags(List.of(tags.get("cardiology"), tags.get("video")));
        contentRepository.save(ecgBasics);
        
        Content heartFailure = createContent(contentRepository, "Heart Failure Management", 
            "Diagnosis and treatment approaches for heart failure", 
            courses.get("cardiology"), ContentType.ARTICLE, "heart_failure.pdf", "application/pdf", 2048L, modules.get("cardioDisorders"), 1);
        heartFailure.setTags(List.of(tags.get("cardiology"), tags.get("pdf")));
        contentRepository.save(heartFailure);
        
        contents.put("skeletalLecture", skeletalLecture);
        contents.put("jointLecture", jointLecture);
        contents.put("boneDevelopment", boneDevelopment);
        contents.put("muscleTypes", muscleTypes);
        contents.put("muscleContraction", muscleContraction);
        contents.put("heartAnatomy", heartAnatomy);
        contents.put("vesselStructure", vesselStructure);
        contents.put("brainAnatomy", brainAnatomy);
        contents.put("spinalCord", spinalCord);
        contents.put("ecgBasics", ecgBasics);
        contents.put("heartFailure", heartFailure);
        
        log.info("Successfully created {} content items", contents.size());
    } catch (Exception e) {
        log.error("Error creating content: " + e.getMessage(), e);
    }
    
    return contents;
}
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Map<String, Module> initModules(ModuleRepository moduleRepository) {
        log.info("Creating course modules...");
        Map<String, Module> modules = new HashMap<>();
        
        try {
            // Anatomy modules
            Module anatomyModule1 = createModule(moduleRepository, "Skeletal System", "Study of bones, cartilage and joints", 1);
            Module anatomyModule2 = createModule(moduleRepository, "Muscular System", "Study of muscles and associated tissues", 2);
            Module anatomyModule3 = createModule(moduleRepository, "Cardiovascular System", "Study of heart and blood vessels", 3);
            Module anatomyModule4 = createModule(moduleRepository, "Nervous System", "Study of brain, spinal cord and nerves", 4);
            
            // Cardiology modules
            Module cardioModule1 = createModule(moduleRepository, "Heart Physiology", "Cardiac function and regulation", 1);
            Module cardioModule2 = createModule(moduleRepository, "Cardiovascular Disorders", "Common heart and vascular diseases", 2);
            
            modules.put("skeletal", anatomyModule1);
            modules.put("muscular", anatomyModule2);
            modules.put("cardiovascular", anatomyModule3);
            modules.put("nervous", anatomyModule4);
            modules.put("heartPhysiology", cardioModule1);
            modules.put("cardioDisorders", cardioModule2);
            
            log.info("Successfully created {} modules", modules.size());
        } catch (Exception e) {
            log.error("Error creating modules: " + e.getMessage(), e);
        }
        
        return modules;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Map<String, Course> initCourses(CourseRepository courseRepository, Map<String, User> users, Map<String, Department> departments) {
        log.info("Creating medical courses...");
        Map<String, Course> courses = new HashMap<>();
        
        try {
            Course anatomyCourse = createCourse(courseRepository, "Human Anatomy Fundamentals", 
                "Comprehensive study of human body structure with focus on clinical applications", 
                users.get("johnson"), departments.get("internalMedicine"), 50);
            
            Course cardiologyCourse = createCourse(courseRepository, "Cardiovascular Medicine", 
                "Diagnosis and management of heart diseases and vascular disorders", 
                users.get("johnson"), departments.get("internalMedicine"), 30);
            
            Course surgicalTechCourse = createCourse(courseRepository, "Surgical Techniques", 
                "Fundamental principles and practices of modern surgery", 
                users.get("chen"), departments.get("surgery"), 25);
            
            Course pediatricCareCourse = createCourse(courseRepository, "Pediatric Patient Care", 
                "Comprehensive approach to treating infants, children and adolescents", 
                users.get("rodriguez"), departments.get("pediatrics"), 40);
            
            Course neuroimagingCourse = createCourse(courseRepository, "Advanced Neuroimaging", 
                "MRI, CT, and PET applications in neurological diagnosis", 
                users.get("williams"), departments.get("radiology"), 20);
            
            Course neurologicalDisordersCourse = createCourse(courseRepository, "Neurological Disorders", 
                "Diagnosis and treatment of common nervous system conditions", 
                users.get("thompson"), departments.get("neurology"), 30);
            
            Course orthopedicSurgeryCourse = createCourse(courseRepository, "Orthopedic Surgery Principles", 
                "Surgical treatment of musculoskeletal trauma and disorders", 
                users.get("chen"), departments.get("surgery"), 35);
            
            Course developmentalMedicineCourse = createCourse(courseRepository, "Developmental Medicine", 
                "Child growth, development and developmental disorders", 
                users.get("rodriguez"), departments.get("pediatrics"), 40);
            
            Course diagnosticImagingCourse = createCourse(courseRepository, "Diagnostic Imaging Fundamentals", 
                "Principles and applications of medical imaging technologies", 
                users.get("williams"), departments.get("radiology"), 45);
            
            Course strokeManagementCourse = createCourse(courseRepository, "Stroke Management", 
                "Acute care and rehabilitation in stroke patients", 
                users.get("thompson"), departments.get("neurology"), 25);
    
            courses.put("anatomy", anatomyCourse);
            courses.put("cardiology", cardiologyCourse);
            courses.put("surgicalTech", surgicalTechCourse);
            courses.put("pediatricCare", pediatricCareCourse);
            courses.put("neuroimaging", neuroimagingCourse);
            courses.put("neurologicalDisorders", neurologicalDisordersCourse);
            courses.put("orthopedicSurgery", orthopedicSurgeryCourse);
            courses.put("developmentalMedicine", developmentalMedicineCourse);
            courses.put("diagnosticImaging", diagnosticImagingCourse);
            courses.put("strokeManagement", strokeManagementCourse);
            
            log.info("Successfully created {} courses", courses.size());
        } catch (Exception e) {
            log.error("Error creating courses: " + e.getMessage(), e);
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
        Department internalMedicine = createDepartment(departmentRepository, "Internal Medicine", "Study of adult diseases", "MED", "Internal Medicine and General Practice", "Dr. Sarah Johnson", "medicine@example.com");
        Department surgery = createDepartment(departmentRepository, "Surgery", "Surgical interventions and procedures", "SURG", "Surgical Specialties", "Dr. Michael Chen", "surgery@example.com");
        Department pediatrics = createDepartment(departmentRepository, "Pediatrics", "Child and adolescent healthcare", "PEDS", "Child and Adolescent Medicine", "Dr. Emily Rodriguez", "pediatrics@example.com");
        Department radiology = createDepartment(departmentRepository, "Radiology", "Medical imaging and diagnostics", "RAD", "Diagnostic Imaging", "Dr. Robert Williams", "radiology@example.com");
        Department neurology = createDepartment(departmentRepository, "Neurology", "Study of nervous system disorders", "NEURO", "Neurological Sciences", "Dr. Lisa Thompson", "neurology@example.com");

        departments.put("internalMedicine", internalMedicine);
        departments.put("surgery", surgery);
        departments.put("pediatrics", pediatrics);
        departments.put("radiology", radiology);
        departments.put("neurology", neurology);
        
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
        
        // Instructors
        User instructorJohnson = createUser(userRepository, "johnson@medlms.com", "Dr. Sarah Johnson", "password123", roles.get("INSTRUCTOR"), departments.get("internalMedicine"));
        User instructorChen = createUser(userRepository, "chen@medlms.com", "Dr. Michael Chen", "password123", roles.get("INSTRUCTOR"), departments.get("surgery"));
        User instructorRodriguez = createUser(userRepository, "rodriguez@medlms.com", "Dr. Emily Rodriguez", "password123", roles.get("INSTRUCTOR"), departments.get("pediatrics"));
        User instructorWilliams = createUser(userRepository, "williams@medlms.com", "Dr. Robert Williams", "password123", roles.get("INSTRUCTOR"), departments.get("radiology"));
        User instructorThompson = createUser(userRepository, "thompson@medlms.com", "Dr. Lisa Thompson", "password123", roles.get("INSTRUCTOR"), departments.get("neurology"));
        
        users.put("johnson", instructorJohnson);
        users.put("chen", instructorChen);
        users.put("rodriguez", instructorRodriguez);
        users.put("williams", instructorWilliams);
        users.put("thompson", instructorThompson);
        
        // Students
        User studentSmith = createUser(userRepository, "smith@student.medlms.com", "John Smith", "password123", roles.get("STUDENT"), departments.get("internalMedicine"));
        User studentBrown = createUser(userRepository, "brown@student.medlms.com", "Maria Brown", "password123", roles.get("STUDENT"), departments.get("surgery"));
        User studentPatel = createUser(userRepository, "patel@student.medlms.com", "Raj Patel", "password123", roles.get("STUDENT"), departments.get("pediatrics"));
        User studentGarcia = createUser(userRepository, "garcia@student.medlms.com", "Sofia Garcia", "password123", roles.get("STUDENT"), departments.get("internalMedicine"));
        User studentKim = createUser(userRepository, "kim@student.medlms.com", "David Kim", "password123", roles.get("STUDENT"), departments.get("radiology"));
        User studentMiller = createUser(userRepository, "miller@student.medlms.com", "James Miller", "password123", roles.get("STUDENT"), departments.get("neurology"));
        User studentWilson = createUser(userRepository, "wilson@student.medlms.com", "Emma Wilson", "password123", roles.get("STUDENT"), departments.get("pediatrics"));
        User studentDavis = createUser(userRepository, "davis@student.medlms.com", "Alex Davis", "password123", roles.get("STUDENT"), departments.get("surgery"));
        User studentJones = createUser(userRepository, "jones@student.medlms.com", "Olivia Jones", "password123", roles.get("STUDENT"), departments.get("neurology"));
        User studentLee = createUser(userRepository, "lee@student.medlms.com", "Daniel Lee", "password123", roles.get("STUDENT"), departments.get("radiology"));
        
        users.put("smith", studentSmith);
        users.put("brown", studentBrown);
        users.put("patel", studentPatel);
        users.put("garcia", studentGarcia);
        users.put("kim", studentKim);
        users.put("miller", studentMiller);
        users.put("wilson", studentWilson);
        users.put("davis", studentDavis);
        users.put("jones", studentJones);
        users.put("lee", studentLee);
        
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
        createUserProfile(userProfileRepository, users.get("johnson"), "Sarah", "Johnson", "123-555-1001", "Professor of Internal Medicine with 15 years of clinical experience. Research focus on cardiovascular health.");
        createUserProfile(userProfileRepository, users.get("chen"), "Michael", "Chen", "123-555-1002", "Chief of Surgery with specialization in minimally invasive procedures.");
        createUserProfile(userProfileRepository, users.get("rodriguez"), "Emily", "Rodriguez", "123-555-1003", "Pediatric specialist with expertise in developmental disorders.");
        createUserProfile(userProfileRepository, users.get("williams"), "Robert", "Williams", "123-555-1004", "Director of Radiology with focus on advanced imaging techniques.");
        createUserProfile(userProfileRepository, users.get("thompson"), "Lisa", "Thompson", "123-555-1005", "Neurologist specializing in stroke treatment and rehabilitation.");
        
        createUserProfile(userProfileRepository, users.get("smith"), "John", "Smith", "123-555-2001", "Third-year medical student interested in cardiology.");
        createUserProfile(userProfileRepository, users.get("brown"), "Maria", "Brown", "123-555-2002", "Fourth-year medical student planning to specialize in orthopedic surgery.");
        createUserProfile(userProfileRepository, users.get("patel"), "Raj", "Patel", "123-555-2003", "Second-year medical student with interest in pediatric oncology.");
        createUserProfile(userProfileRepository, users.get("garcia"), "Sofia", "Garcia", "123-555-2004", "Third-year medical student focusing on internal medicine and infectious diseases.");
        createUserProfile(userProfileRepository, users.get("kim"), "David", "Kim", "123-555-2005", "Fourth-year medical student pursuing a career in interventional radiology.");
        createUserProfile(userProfileRepository, users.get("miller"), "James", "Miller", "123-555-2006", "Second-year medical student interested in neurosurgery.");
        createUserProfile(userProfileRepository, users.get("wilson"), "Emma", "Wilson", "123-555-2007", "Third-year medical student with focus on pediatric emergency medicine.");
        createUserProfile(userProfileRepository, users.get("davis"), "Alex", "Davis", "123-555-2008", "Fourth-year medical student planning to specialize in plastic surgery.");
        createUserProfile(userProfileRepository, users.get("jones"), "Olivia", "Jones", "123-555-2009", "Second-year medical student interested in neuropsychiatry.");
        createUserProfile(userProfileRepository, users.get("lee"), "Daniel", "Lee", "123-555-2010", "Third-year medical student focusing on diagnostic radiology and nuclear medicine.");
        
        log.info("Successfully created user profiles");
    } catch (Exception e) {
        log.error("Error creating user profiles: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initAnswerOptions(AnswerOptionRepository answerOptionRepository, Map<String, Question> questions) {
    log.info("Creating answer options...");
    
    try {
        // Answer options for q1
        createAnswerOption(answerOptionRepository, "Scaphoid", false, "The scaphoid is one of the carpal bones.", 1, questions.get("q1"));
        createAnswerOption(answerOptionRepository, "Lunate", false, "The lunate is one of the carpal bones.", 2, questions.get("q1"));
        createAnswerOption(answerOptionRepository, "Metacarpal", true, "Correct! Metacarpals are the bones in the hand, not the wrist.", 3, questions.get("q1"));
        createAnswerOption(answerOptionRepository, "Pisiform", false, "The pisiform is one of the carpal bones.", 4, questions.get("q1"));
        
        // Answer options for q2
        createAnswerOption(answerOptionRepository, "Pelvis", true, "Correct! The femur articulates with the acetabulum of the pelvis.", 1, questions.get("q2"));
        createAnswerOption(answerOptionRepository, "Tibia", true, "Correct! The femur articulates with the tibia at the knee joint.", 2, questions.get("q2"));
        createAnswerOption(answerOptionRepository, "Fibula", false, "The fibula does not directly articulate with the femur.", 3, questions.get("q2"));
        createAnswerOption(answerOptionRepository, "Patella", false, "The patella is a sesamoid bone within the quadriceps tendon that articulates with the femur but is not considered a primary articulation.", 4, questions.get("q2"));
        
        // Answer options for q3
        createAnswerOption(answerOptionRepository, "Humerus", false, "The biceps brachii originates on the scapula, not the humerus.", 1, questions.get("q3"));
        createAnswerOption(answerOptionRepository, "Scapula", true, "Correct! The biceps brachii has two heads that originate on the scapula.", 2, questions.get("q3"));
        createAnswerOption(answerOptionRepository, "Radius", false, "The radius is where the biceps brachii inserts, not where it originates.", 3, questions.get("q3"));
        createAnswerOption(answerOptionRepository, "Ulna", false, "The biceps brachii does not originate on the ulna.", 4, questions.get("q3"));
        
        // Answer options for q4
        createAnswerOption(answerOptionRepository, "2", false, "The human heart has 4 chambers, not 2.", 1, questions.get("q4"));
        createAnswerOption(answerOptionRepository, "3", false, "The human heart has 4 chambers, not 3.", 2, questions.get("q4"));
        createAnswerOption(answerOptionRepository, "4", true, "Correct! The human heart has 4 chambers: right atrium, right ventricle, left atrium, and left ventricle.", 3, questions.get("q4"));
        createAnswerOption(answerOptionRepository, "5", false, "The human heart has 4 chambers, not 5.", 4, questions.get("q4"));
        
        // Answer options for q5 (essay question)
        createAnswerOption(answerOptionRepository, "The blood-brain barrier is a highly selective semipermeable border of endothelial cells that prevents solutes in the circulating blood from non-selectively crossing into the extracellular fluid of the central nervous system where neurons reside. It is formed by brain endothelial cells connected by tight junctions and serves to protect the brain from pathogens, toxins, and hormones while allowing essential nutrients to pass through.", true, "Key points to include: selective permeability, endothelial cells, tight junctions, protection of CNS, allowing nutrients to pass.", 1, questions.get("q5"));
        
        // Answer options for cardiology questions
        createAnswerOption(answerOptionRepository, "Ventricular depolarization", false, "Ventricular depolarization is represented by the QRS complex.", 1, questions.get("cq1"));
        createAnswerOption(answerOptionRepository, "Atrial depolarization", true, "Correct! The P wave represents atrial depolarization.", 2, questions.get("cq1"));
        createAnswerOption(answerOptionRepository, "Ventricular repolarization", false, "Ventricular repolarization is represented by the T wave.", 3, questions.get("cq1"));
        createAnswerOption(answerOptionRepository, "Atrial repolarization", false, "Atrial repolarization is usually obscured by the QRS complex.", 4, questions.get("cq1"));
        
        createAnswerOption(answerOptionRepository, "ACE inhibitors", false, "ACE inhibitors are commonly used to treat heart failure.", 1, questions.get("cq2"));
        createAnswerOption(answerOptionRepository, "Beta-blockers", false, "Beta-blockers are commonly used to treat heart failure.", 2, questions.get("cq2"));
        createAnswerOption(answerOptionRepository, "Warfarin", true, "Correct! Warfarin is an anticoagulant, not a primary heart failure medication.", 3, questions.get("cq2"));
        createAnswerOption(answerOptionRepository, "Diuretics", false, "Diuretics are commonly used to treat heart failure.", 4, questions.get("cq2"));
        
        log.info("Successfully created answer options");
    } catch (Exception e) {
        log.error("Error creating answer options: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, QuizAttempt> initQuizAttempts(QuizAttemptRepository quizAttemptRepository, Map<String, Quiz> quizzes, Map<String, User> users) {
    log.info("Creating quiz attempts...");
    Map<String, QuizAttempt> attempts = new HashMap<>();
    
    try {
        // Create quiz attempts
        QuizAttempt smithAnatomyAttempt = createQuizAttempt(quizAttemptRepository, quizzes.get("anatomyQuiz"), 
            users.get("smith"), LocalDateTime.now().minusDays(39).plusHours(1), 
            LocalDateTime.now().minusDays(39).plusHours(2), 80.0, 80.0, true, AttemptStatus.COMPLETED);
        
        QuizAttempt brownAnatomyAttempt = createQuizAttempt(quizAttemptRepository, quizzes.get("anatomyQuiz"), 
            users.get("brown"), LocalDateTime.now().minusDays(39).plusHours(2), 
            LocalDateTime.now().minusDays(39).plusHours(3), 85.0, 85.0, true, AttemptStatus.COMPLETED);
        
        QuizAttempt garciaAnatomyAttempt = createQuizAttempt(quizAttemptRepository, quizzes.get("anatomyQuiz"), 
            users.get("garcia"), LocalDateTime.now().minusDays(39).plusHours(3), 
            LocalDateTime.now().minusDays(39).plusHours(4), 65.0, 65.0, false, AttemptStatus.COMPLETED);
        
        attempts.put("smithAnatomyAttempt", smithAnatomyAttempt);
        attempts.put("brownAnatomyAttempt", brownAnatomyAttempt);
        attempts.put("garciaAnatomyAttempt", garciaAnatomyAttempt);
        
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
        // Create student answers for Smith's anatomy quiz attempt
        createStudentAnswer(studentAnswerRepository, attempts.get("smithAnatomyAttempt"), questions.get("q1"), 
            List.of(answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(questions.get("q1").getId()).get(2)), 
            null, 5.0, true, false, null);
        
        createStudentAnswer(studentAnswerRepository, attempts.get("smithAnatomyAttempt"), questions.get("q2"), 
            List.of(answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(questions.get("q2").getId()).get(0), 
                   answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(questions.get("q2").getId()).get(1)), 
            null, 5.0, true, false, null);
        
        createStudentAnswer(studentAnswerRepository, attempts.get("smithAnatomyAttempt"), questions.get("q3"), 
            List.of(answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(questions.get("q3").getId()).get(1)), 
            null, 5.0, true, false, null);
        
        createStudentAnswer(studentAnswerRepository, attempts.get("smithAnatomyAttempt"), questions.get("q4"), 
            List.of(answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(questions.get("q4").getId()).get(2)), 
            null, 5.0, true, false, null);
        
        createStudentAnswer(studentAnswerRepository, attempts.get("smithAnatomyAttempt"), questions.get("q5"), 
            List.of(), 
            "The blood-brain barrier is a protective mechanism that prevents harmful substances from entering the brain while allowing necessary nutrients to pass through. It consists of tightly packed endothelial cells that form the walls of brain capillaries, with tight junctions between them that restrict passage of most molecules.", 
            8.0, true, true, "Good explanation of the structure and function, but could include more details about the specific transport mechanisms.");
        
        log.info("Successfully created student answers");
    } catch (Exception e) {
        log.error("Error creating student answers: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, Assignment> initAssignments(AssignmentRepository assignmentRepository, Map<String, Course> courses) {
    log.info("Creating assignments...");
    Map<String, Assignment> assignments = new HashMap<>();
    
    try {
        Assignment anatomyAssignment = createAssignment(assignmentRepository, courses.get("anatomy"), 
            "Skeletal System Diagram", 
            "Create a detailed diagram of the human skeletal system, labeling all major bones and joints. Include a brief description of the function of each bone group.", 
            LocalDateTime.now().minusDays(50), 100);
        
        Assignment cardiologyAssignment = createAssignment(assignmentRepository, courses.get("cardiology"), 
            "ECG Analysis Case Study", 
            "Analyze the provided ECG tracings and identify the cardiac abnormalities present. Provide a clinical interpretation and suggested treatment approach for each case.", 
            LocalDateTime.now().minusDays(15), 100);
        
        Assignment neurologyAssignment = createAssignment(assignmentRepository, courses.get("neurologicalDisorders"), 
            "Neurological Case Report", 
            "Write a detailed case report on a patient with a neurological disorder of your choice. Include pathophysiology, clinical presentation, diagnostic approach, and treatment plan.", 
            LocalDateTime.now().minusDays(25), 100);
        
        assignments.put("anatomyAssignment", anatomyAssignment);
        assignments.put("cardiologyAssignment", cardiologyAssignment);
        assignments.put("neurologyAssignment", neurologyAssignment);
        
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
        createScore(scoreRepository, users.get("smith").getId(), assignments.get("anatomyAssignment").getId(), 92, LocalDateTime.now().minusDays(45));
        createScore(scoreRepository, users.get("brown").getId(), assignments.get("anatomyAssignment").getId(), 88, LocalDateTime.now().minusDays(44));
        createScore(scoreRepository, users.get("patel").getId(), assignments.get("anatomyAssignment").getId(), 95, LocalDateTime.now().minusDays(46));
        createScore(scoreRepository, users.get("garcia").getId(), assignments.get("anatomyAssignment").getId(), 78, LocalDateTime.now().minusDays(45));
        createScore(scoreRepository, users.get("smith").getId(), assignments.get("cardiologyAssignment").getId(), 85, LocalDateTime.now().minusDays(10));
        createScore(scoreRepository, users.get("jones").getId(), assignments.get("neurologyAssignment").getId(), 90, LocalDateTime.now().minusDays(20));
        
        log.info("Successfully created assignment scores");
    } catch (Exception e) {
        log.error("Error creating assignment scores: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initSubmissions(SubmissionRepository submissionRepository, Map<String, Assignment> assignments, Map<String, User> users) {
    log.info("Creating assignment submissions...");
    
    try {
        createSubmission(submissionRepository, assignments.get("anatomyAssignment").getId(), users.get("smith").getId(), 
            "smith_anatomy_assignment.pdf", LocalDateTime.now().minusDays(48));
        
        createSubmission(submissionRepository, assignments.get("anatomyAssignment").getId(), users.get("brown").getId(), 
            "brown_anatomy_assignment.pdf", LocalDateTime.now().minusDays(49));
        
        createSubmission(submissionRepository, assignments.get("cardiologyAssignment").getId(), users.get("smith").getId(), 
            "smith_cardiology_assignment.pdf", LocalDateTime.now().minusDays(12));
        
        createSubmission(submissionRepository, assignments.get("neurologyAssignment").getId(), users.get("jones").getId(), 
            "jones_neurology_assignment.pdf", LocalDateTime.now().minusDays(22));
        
        log.info("Successfully created assignment submissions");
    } catch (Exception e) {
        log.error("Error creating assignment submissions: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private Map<String, ForumThread> initForumThreads(ForumThreadRepository forumThreadRepository, Map<String, Course> courses, Map<String, User> users) {
    log.info("Creating forum threads...");
    Map<String, ForumThread> threads = new HashMap<>();
    
    try {
        ForumThread anatomyThread = createForumThread(forumThreadRepository, courses.get("anatomy").getId(), 
            "Difficulty understanding joint classifications", users.get("smith").getId(), LocalDateTime.now().minusDays(52));
        
        ForumThread cardiologyThread = createForumThread(forumThreadRepository, courses.get("cardiology").getId(), 
            "Question about heart valve sounds", users.get("garcia").getId(), LocalDateTime.now().minusDays(25));
        
        ForumThread pediatricsThread = createForumThread(forumThreadRepository, courses.get("pediatricCare").getId(), 
            "Developmental milestones reference", users.get("wilson").getId(), LocalDateTime.now().minusDays(35));
        
        threads.put("anatomyThread", anatomyThread);
        threads.put("cardiologyThread", cardiologyThread);
        threads.put("pediatricsThread", pediatricsThread);
        
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
        // Anatomy thread posts
        createForumPost(forumPostRepository, threads.get("anatomyThread").getId(), users.get("smith").getId(), 
            "I'm having trouble understanding the classification of joints, particularly the difference between functional and structural classifications. Can someone explain this in simpler terms?", 
            LocalDateTime.now().minusDays(52));
        
        createForumPost(forumPostRepository, threads.get("anatomyThread").getId(), users.get("johnson").getId(), 
            "Great question! Structural classification refers to how the joints are physically connected (fibrous, cartilaginous, synovial), while functional classification refers to how much movement they allow (synarthrosis, amphiarthrosis, diarthrosis). For example, a suture in the skull is structurally fibrous and functionally a synarthrosis (immovable).", 
            LocalDateTime.now().minusDays(51));
        
        createForumPost(forumPostRepository, threads.get("anatomyThread").getId(), users.get("brown").getId(), 
            "I found this helpful diagram showing examples of each type: [link to diagram]. It really helped me understand the differences.", 
            LocalDateTime.now().minusDays(50));
        
        createForumPost(forumPostRepository, threads.get("anatomyThread").getId(), users.get("smith").getId(), 
            "Thank you both! That clarifies it for me. @Dr. Johnson, do we need to know specific examples of each type for the exam?", 
            LocalDateTime.now().minusDays(49));
        
        createForumPost(forumPostRepository, threads.get("anatomyThread").getId(), users.get("johnson").getId(), 
            "Yes, you should be familiar with common examples of each type. Review the examples in Chapter 8 and the lab slides from week 3.", 
            LocalDateTime.now().minusDays(48));
        
        // Cardiology thread posts
        createForumPost(forumPostRepository, threads.get("cardiologyThread").getId(), users.get("garcia").getId(), 
            "I'm confused about the difference between S1 and S2 heart sounds. How can you distinguish them when auscultating?", 
            LocalDateTime.now().minusDays(25));
        
        createForumPost(forumPostRepository, threads.get("cardiologyThread").getId(), users.get("johnson").getId(), 
            "S1 ('lub') is caused by closure of the mitral and tricuspid valves at the beginning of systole, while S2 ('dub') is caused by closure of the aortic and pulmonary valves at the beginning of diastole. S1 is usually louder at the apex of the heart, while S2 is louder at the base. Also, S1 is generally longer and lower pitched than S2.", 
            LocalDateTime.now().minusDays(24));
        
        // Pediatrics thread posts
        createForumPost(forumPostRepository, threads.get("pediatricsThread").getId(), users.get("wilson").getId(), 
            "Does anyone have a good reference chart for developmental milestones? I'm finding conflicting information in different sources.", 
            LocalDateTime.now().minusDays(35));
        
        createForumPost(forumPostRepository, threads.get("pediatricsThread").getId(), users.get("rodriguez").getId(), 
            "The CDC has an excellent reference guide that we use in clinical practice. I'll upload it to the course resources section. Remember that these are guidelines and there's normal variation in development.", 
            LocalDateTime.now().minusDays(34));
        
        createForumPost(forumPostRepository, threads.get("pediatricsThread").getId(), users.get("patel").getId(), 
            "I've found the WHO growth charts to be reliable as well, especially for global comparisons.", 
            LocalDateTime.now().minusDays(33));
        
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
        createNotification(notificationRepository, NotificationType.COURSE_CONTENT_UPLOAD, users.get("smith"), 
            "New Content: Heart Anatomy", 
            "New content has been added to your course: Cardiovascular Medicine", 
            courses.get("cardiology").getId(), "course");
        
        createNotification(notificationRepository, NotificationType.ASSIGNMENT_DEADLINE_24H, users.get("smith"), 
            "Assignment Due in 24 Hours", 
            "Your assignment 'ECG Analysis Case Study' is due soon.", 
            assignments.get("cardiologyAssignment").getId(), "assignment");
        
        createNotification(notificationRepository, NotificationType.QUIZ_AVAILABLE, users.get("brown"), 
            "New Quiz Available: Human Anatomy Midterm", 
            "A new quiz is available in your course: Human Anatomy Fundamentals", 
            quizzes.get("anatomyQuiz").getId(), "quiz");
        
        createNotification(notificationRepository, NotificationType.FORUM_REPLY, users.get("garcia"), 
            "New Reply to Your Forum Post", 
            "Dr. Sarah Johnson replied to your post: \"I'm confused about the difference between S1 and S2 heart sounds...\"", 
            threads.get("cardiologyThread").getId(), "forumPost");
        
        createNotification(notificationRepository, NotificationType.GRADE_POSTED, users.get("smith"), 
            "Grade Posted: Skeletal System Diagram", 
            "Your grade for Skeletal System Diagram in Human Anatomy Fundamentals has been posted.", 
            assignments.get("anatomyAssignment").getId(), "assignment");
        
        log.info("Successfully created notifications");
    } catch (Exception e) {
        log.error("Error creating notifications: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initNotificationPreferences(NotificationPreferenceRepository notificationPreferenceRepository, Map<String, User> users) {
    log.info("Creating notification preferences...");
    
    try {
        createNotificationPreference(notificationPreferenceRepository, users.get("smith"), NotificationType.COURSE_CONTENT_UPLOAD, true, true);
        createNotificationPreference(notificationPreferenceRepository, users.get("smith"), NotificationType.ASSIGNMENT_DEADLINE_24H, true, true);
        createNotificationPreference(notificationPreferenceRepository, users.get("smith"), NotificationType.QUIZ_AVAILABLE, true, true);
        createNotificationPreference(notificationPreferenceRepository, users.get("smith"), NotificationType.FORUM_REPLY, true, true);
        createNotificationPreference(notificationPreferenceRepository, users.get("smith"), NotificationType.GRADE_POSTED, true, true);
        
        createNotificationPreference(notificationPreferenceRepository, users.get("brown"), NotificationType.COURSE_CONTENT_UPLOAD, true, false);
        createNotificationPreference(notificationPreferenceRepository, users.get("brown"), NotificationType.ASSIGNMENT_DEADLINE_24H, true, true);
        
        createNotificationPreference(notificationPreferenceRepository, users.get("patel"), NotificationType.FORUM_REPLY, true, true);
        createNotificationPreference(notificationPreferenceRepository, users.get("patel"), NotificationType.GRADE_POSTED, true, false);
        
        log.info("Successfully created notification preferences");
    } catch (Exception e) {
        log.error("Error creating notification preferences: " + e.getMessage(), e);
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void initCertificates(CertificateRepository certificateRepository, Map<String, User> users, Map<String, Course> courses) {
    log.info("Creating certificates...");
    
    try {
        createCertificate(certificateRepository, users.get("smith").getId(), courses.get("anatomy").getId(), 
            "certificate_smith_anatomy.pdf", LocalDateTime.now().minusDays(14), "Human Anatomy Fundamentals");
        
        createCertificate(certificateRepository, users.get("brown").getId(), courses.get("anatomy").getId(), 
            "certificate_brown_anatomy.pdf", LocalDateTime.now().minusDays(12), "Human Anatomy Fundamentals");
        
        createCertificate(certificateRepository, users.get("patel").getId(), courses.get("anatomy").getId(), 
            "certificate_patel_anatomy.pdf", LocalDateTime.now().minusDays(11), "Human Anatomy Fundamentals");
        
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
    private Assignment createAssignment(AssignmentRepository repository, Course course, String title, String description, LocalDateTime dueDate, Integer maxScore) {
        Assignment assignment = new Assignment();
        assignment.setCourseId(course.getId());
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
}