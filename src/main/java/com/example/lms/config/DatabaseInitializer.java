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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

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
            log.info("Loading Healthcare LMS database with test data...");

            // Check if data already exists
            if (userRepository.count() > 0) {
                log.info("Database already contains data, skipping initialization.");
                return;
            }

            // Create departments
            log.info("Creating medical departments...");
            Department internalMedicine = createDepartment(departmentRepository, "Internal Medicine", "Study of adult diseases", "MED", "Internal Medicine and General Practice", "Dr. Sarah Johnson", "medicine@example.com");
            Department surgery = createDepartment(departmentRepository, "Surgery", "Surgical interventions and procedures", "SURG", "Surgical Specialties", "Dr. Michael Chen", "surgery@example.com");
            Department pediatrics = createDepartment(departmentRepository, "Pediatrics", "Child and adolescent healthcare", "PEDS", "Child and Adolescent Medicine", "Dr. Emily Rodriguez", "pediatrics@example.com");
            Department radiology = createDepartment(departmentRepository, "Radiology", "Medical imaging and diagnostics", "RAD", "Diagnostic Imaging", "Dr. Robert Williams", "radiology@example.com");
            Department neurology = createDepartment(departmentRepository, "Neurology", "Study of nervous system disorders", "NEURO", "Neurological Sciences", "Dr. Lisa Thompson", "neurology@example.com");

            // Create roles and permissions
            log.info("Creating roles and permissions...");
            Map<String, Role> roles = createRolesAndPermissions(roleRepository, permissionRepository);

            // Create users
            log.info("Creating users...");
            User adminUser = createUser(userRepository, "admin@medlms.com", "Admin User", "password123", roles.get("ADMIN"), null);
            
            // Instructors
            User instructorJohnson = createUser(userRepository, "johnson@medlms.com", "Dr. Sarah Johnson", "password123", roles.get("INSTRUCTOR"), internalMedicine);
            User instructorChen = createUser(userRepository, "chen@medlms.com", "Dr. Michael Chen", "password123", roles.get("INSTRUCTOR"), surgery);
            User instructorRodriguez = createUser(userRepository, "rodriguez@medlms.com", "Dr. Emily Rodriguez", "password123", roles.get("INSTRUCTOR"), pediatrics);
            User instructorWilliams = createUser(userRepository, "williams@medlms.com", "Dr. Robert Williams", "password123", roles.get("INSTRUCTOR"), radiology);
            User instructorThompson = createUser(userRepository, "thompson@medlms.com", "Dr. Lisa Thompson", "password123", roles.get("INSTRUCTOR"), neurology);
            
            // Students
            User studentSmith = createUser(userRepository, "smith@student.medlms.com", "John Smith", "password123", roles.get("STUDENT"), internalMedicine);
            User studentBrown = createUser(userRepository, "brown@student.medlms.com", "Maria Brown", "password123", roles.get("STUDENT"), surgery);
            User studentPatel = createUser(userRepository, "patel@student.medlms.com", "Raj Patel", "password123", roles.get("STUDENT"), pediatrics);
            User studentGarcia = createUser(userRepository, "garcia@student.medlms.com", "Sofia Garcia", "password123", roles.get("STUDENT"), internalMedicine);
            User studentKim = createUser(userRepository, "kim@student.medlms.com", "David Kim", "password123", roles.get("STUDENT"), radiology);
            User studentMiller = createUser(userRepository, "miller@student.medlms.com", "James Miller", "password123", roles.get("STUDENT"), neurology);
            User studentWilson = createUser(userRepository, "wilson@student.medlms.com", "Emma Wilson", "password123", roles.get("STUDENT"), pediatrics);
            User studentDavis = createUser(userRepository, "davis@student.medlms.com", "Alex Davis", "password123", roles.get("STUDENT"), surgery);
            User studentJones = createUser(userRepository, "jones@student.medlms.com", "Olivia Jones", "password123", roles.get("STUDENT"), neurology);
            User studentLee = createUser(userRepository, "lee@student.medlms.com", "Daniel Lee", "password123", roles.get("STUDENT"), radiology);

            // Create user profiles
            log.info("Creating user profiles...");
            createUserProfile(userProfileRepository, adminUser, "Admin", "User", "123-456-7890", "System administrator with technical expertise.");
            createUserProfile(userProfileRepository, instructorJohnson, "Sarah", "Johnson", "123-555-1001", "Professor of Internal Medicine with 15 years of clinical experience. Research focus on cardiovascular health.");
            createUserProfile(userProfileRepository, instructorChen, "Michael", "Chen", "123-555-1002", "Chief of Surgery with specialization in minimally invasive procedures.");
            createUserProfile(userProfileRepository, instructorRodriguez, "Emily", "Rodriguez", "123-555-1003", "Pediatric specialist with expertise in developmental disorders.");
            createUserProfile(userProfileRepository, instructorWilliams, "Robert", "Williams", "123-555-1004", "Director of Radiology with focus on advanced imaging techniques.");
            createUserProfile(userProfileRepository, instructorThompson, "Lisa", "Thompson", "123-555-1005", "Neurologist specializing in stroke treatment and rehabilitation.");
            
            createUserProfile(userProfileRepository, studentSmith, "John", "Smith", "123-555-2001", "Third-year medical student interested in cardiology.");
            createUserProfile(userProfileRepository, studentBrown, "Maria", "Brown", "123-555-2002", "Fourth-year medical student planning to specialize in orthopedic surgery.");
            createUserProfile(userProfileRepository, studentPatel, "Raj", "Patel", "123-555-2003", "Second-year medical student with interest in pediatric oncology.");
            createUserProfile(userProfileRepository, studentGarcia, "Sofia", "Garcia", "123-555-2004", "Third-year medical student focusing on internal medicine and infectious diseases.");
            createUserProfile(userProfileRepository, studentKim, "David", "Kim", "123-555-2005", "Fourth-year medical student pursuing a career in interventional radiology.");
            createUserProfile(userProfileRepository, studentMiller, "James", "Miller", "123-555-2006", "Second-year medical student interested in neurosurgery.");
            createUserProfile(userProfileRepository, studentWilson, "Emma", "Wilson", "123-555-2007", "Third-year medical student with focus on pediatric emergency medicine.");
            createUserProfile(userProfileRepository, studentDavis, "Alex", "Davis", "123-555-2008", "Fourth-year medical student planning to specialize in plastic surgery.");
            createUserProfile(userProfileRepository, studentJones, "Olivia", "Jones", "123-555-2009", "Second-year medical student interested in neuropsychiatry.");
            createUserProfile(userProfileRepository, studentLee, "Daniel", "Lee", "123-555-2010", "Third-year medical student focusing on diagnostic radiology and nuclear medicine.");

            // Create courses
            log.info("Creating medical courses...");
            Course anatomyCourse = createCourse(courseRepository, "Human Anatomy Fundamentals", "Comprehensive study of human body structure with focus on clinical applications", instructorJohnson, internalMedicine, 50);
            Course cardiologyCourse = createCourse(courseRepository, "Cardiovascular Medicine", "Diagnosis and management of heart diseases and vascular disorders", instructorJohnson, internalMedicine, 30);
            Course surgicalTechCourse = createCourse(courseRepository, "Surgical Techniques", "Fundamental principles and practices of modern surgery", instructorChen, surgery, 25);
            Course pediatricCareCourse = createCourse(courseRepository, "Pediatric Patient Care", "Comprehensive approach to treating infants, children and adolescents", instructorRodriguez, pediatrics, 40);
            Course neuroimagingCourse = createCourse(courseRepository, "Advanced Neuroimaging", "MRI, CT, and PET applications in neurological diagnosis", instructorWilliams, radiology, 20);
            Course neurologicalDisordersCourse = createCourse(courseRepository, "Neurological Disorders", "Diagnosis and treatment of common nervous system conditions", instructorThompson, neurology, 30);
            Course orthopedicSurgeryCourse = createCourse(courseRepository, "Orthopedic Surgery Principles", "Surgical treatment of musculoskeletal trauma and disorders", instructorChen, surgery, 35);
            Course developmentalMedicineCourse = createCourse(courseRepository, "Developmental Medicine", "Child growth, development and developmental disorders", instructorRodriguez, pediatrics, 40);
            Course diagnosticImagingCourse = createCourse(courseRepository, "Diagnostic Imaging Fundamentals", "Principles and applications of medical imaging technologies", instructorWilliams, radiology, 45);
            Course strokeManagementCourse = createCourse(courseRepository, "Stroke Management", "Acute care and rehabilitation in stroke patients", instructorThompson, neurology, 25);

            // Set course prerequisites
            courseRepository.save(cardiologyCourse);
            cardiologyCourse.setPrerequisites(Set.of(anatomyCourse));
            courseRepository.save(cardiologyCourse);

            courseRepository.save(surgicalTechCourse);
            surgicalTechCourse.setPrerequisites(Set.of(anatomyCourse));
            courseRepository.save(surgicalTechCourse);

            courseRepository.save(strokeManagementCourse);
            strokeManagementCourse.setPrerequisites(Set.of(neurologicalDisordersCourse));
            courseRepository.save(strokeManagementCourse);

            courseRepository.save(orthopedicSurgeryCourse);
            orthopedicSurgeryCourse.setPrerequisites(Set.of(anatomyCourse, surgicalTechCourse));
            courseRepository.save(orthopedicSurgeryCourse);

            // Create module content
            log.info("Creating course modules and content...");
            // Create modules for Anatomy course
            Module anatomyModule1 = createModule(moduleRepository, "Skeletal System", "Study of bones, cartilage and joints", 1);
            Module anatomyModule2 = createModule(moduleRepository, "Muscular System", "Study of muscles and associated tissues", 2);
            Module anatomyModule3 = createModule(moduleRepository, "Cardiovascular System", "Study of heart and blood vessels", 3);
            Module anatomyModule4 = createModule(moduleRepository, "Nervous System", "Study of brain, spinal cord and nerves", 4);
            
            // Create tags
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

            // Create content for Anatomy modules
            Content skeletalLecture = createContent(contentRepository, "Skeletal System Overview", "Comprehensive introduction to the human skeletal system", anatomyCourse, ContentType.VIDEO, "skeletal_system.mp4", "video/mp4", 1024L, anatomyModule1, 1);
            skeletalLecture.setTags(List.of(anatomyTag, lectureTag, videoTag));
            contentRepository.save(skeletalLecture);
            
            Content jointLecture = createContent(contentRepository, "Joint Structure and Function", "Detailed examination of synovial, fibrous and cartilaginous joints", anatomyCourse, ContentType.VIDEO, "joints.mp4", "video/mp4", 1536L, anatomyModule1, 2);
            jointLecture.setTags(List.of(anatomyTag, lectureTag, videoTag));
            contentRepository.save(jointLecture);
            
            Content boneDevelopment = createContent(contentRepository, "Bone Development", "Ossification process and bone growth through lifespan", anatomyCourse, ContentType.ARTICLE, "bone_development.pdf", "application/pdf", 2048L, anatomyModule1, 3);
            boneDevelopment.setTags(List.of(anatomyTag, pdfTag));
            contentRepository.save(boneDevelopment);
            
            Content muscleTypes = createContent(contentRepository, "Types of Muscle Tissue", "Skeletal, cardiac and smooth muscle structure and function", anatomyCourse, ContentType.VIDEO, "muscle_types.mp4", "video/mp4", 1280L, anatomyModule2, 1);
            muscleTypes.setTags(List.of(anatomyTag, lectureTag, videoTag));
            contentRepository.save(muscleTypes);
            
            Content muscleContraction = createContent(contentRepository, "Muscle Contraction", "Sliding filament theory and excitation-contraction coupling", anatomyCourse, ContentType.ARTICLE, "muscle_contraction.pdf", "application/pdf", 1792L, anatomyModule2, 2);
            muscleContraction.setTags(List.of(anatomyTag, pdfTag));
            contentRepository.save(muscleContraction);
            
            Content heartAnatomy = createContent(contentRepository, "Heart Anatomy", "Chambers, valves and cardiac circulation", anatomyCourse, ContentType.VIDEO, "heart_anatomy.mp4", "video/mp4", 2304L, anatomyModule3, 1);
            heartAnatomy.setTags(List.of(anatomyTag, cardiologyTag, videoTag));
            contentRepository.save(heartAnatomy);
            
            Content vesselStructure = createContent(contentRepository, "Blood Vessel Structure", "Arteries, veins and capillaries - wall structure and specializations", anatomyCourse, ContentType.ARTICLE, "vessel_structure.pdf", "application/pdf", 1536L, anatomyModule3, 2);
            vesselStructure.setTags(List.of(anatomyTag, cardiologyTag, pdfTag));
            contentRepository.save(vesselStructure);
            
            Content brainAnatomy = createContent(contentRepository, "Brain Anatomy", "Cerebrum, cerebellum, brain stem and functional areas", anatomyCourse, ContentType.VIDEO, "brain_anatomy.mp4", "video/mp4", 2560L, anatomyModule4, 1);
            brainAnatomy.setTags(List.of(anatomyTag, neurologyTag, videoTag));
            contentRepository.save(brainAnatomy);
            
            Content spinalCord = createContent(contentRepository, "Spinal Cord and Nerves", "Spinal tracts, reflexes and peripheral nervous system", anatomyCourse, ContentType.ARTICLE, "spinal_cord.pdf", "application/pdf", 1792L, anatomyModule4, 2);
            spinalCord.setTags(List.of(anatomyTag, neurologyTag, pdfTag));
            contentRepository.save(spinalCord);
            
            // Create content for other courses
            // Cardiology course content
            Module cardioModule1 = createModule(moduleRepository, "Heart Physiology", "Cardiac function and regulation", 1);
            Module cardioModule2 = createModule(moduleRepository, "Cardiovascular Disorders", "Common heart and vascular diseases", 2);
            
            Content ecgBasics = createContent(contentRepository, "ECG Interpretation Basics", "Fundamentals of electrocardiogram reading and analysis", cardiologyCourse, ContentType.VIDEO, "ecg_basics.mp4", "video/mp4", 1848L, cardioModule1, 1);
            ecgBasics.setTags(List.of(cardiologyTag, videoTag));
            contentRepository.save(ecgBasics);
            
            Content heartFailure = createContent(contentRepository, "Heart Failure Management", "Diagnosis and treatment approaches for heart failure", cardiologyCourse, ContentType.ARTICLE, "heart_failure.pdf", "application/pdf", 2048L, cardioModule2, 1);
            heartFailure.setTags(List.of(cardiologyTag, pdfTag));
            contentRepository.save(heartFailure);

            // Create enrollments
            log.info("Creating course enrollments...");
            // Enroll students in Anatomy (most basic course)
            Enrollment smithAnatomyEnrollment = createEnrollment(enrollmentRepository, studentSmith, anatomyCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(60), 75.0);
            Enrollment brownAnatomyEnrollment = createEnrollment(enrollmentRepository, studentBrown, anatomyCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(58), 85.0);
            Enrollment patelAnatomyEnrollment = createEnrollment(enrollmentRepository, studentPatel, anatomyCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(62), 90.0);
            Enrollment garciaAnatomyEnrollment = createEnrollment(enrollmentRepository, studentGarcia, anatomyCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(59), 70.0);
            Enrollment kimAnatomyEnrollment = createEnrollment(enrollmentRepository, studentKim, anatomyCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(57), 95.0);
            
            // Enroll students in Cardiology (after completing anatomy)
            Enrollment smithCardiologyEnrollment = createEnrollment(enrollmentRepository, studentSmith, cardiologyCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(30), 65.0);
            Enrollment patelCardiologyEnrollment = createEnrollment(enrollmentRepository, studentPatel, cardiologyCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(32), 80.0);
            Enrollment garciaCardiologyEnrollment = createEnrollment(enrollmentRepository, studentGarcia, cardiologyCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(28), 60.0);
            
            // Enroll students in Surgical Techniques
            Enrollment brownSurgicalEnrollment = createEnrollment(enrollmentRepository, studentBrown, surgicalTechCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(25), 78.0);
            Enrollment kimSurgicalEnrollment = createEnrollment(enrollmentRepository, studentKim, surgicalTechCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(22), 92.0);
            
            // Enroll students in Pediatric Care
            Enrollment millerPediatricEnrollment = createEnrollment(enrollmentRepository, studentMiller, pediatricCareCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(40), 85.0);
            Enrollment wilsonPediatricEnrollment = createEnrollment(enrollmentRepository, studentWilson, pediatricCareCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(39), 75.0);
            
            // Additional enrollments
            Enrollment davisOrthoEnrollment = createEnrollment(enrollmentRepository, studentDavis, orthopedicSurgeryCourse, EnrollmentStatus.PENDING, LocalDateTime.now().minusDays(5), 0.0);
            Enrollment jonesNeuroEnrollment = createEnrollment(enrollmentRepository, studentJones, neurologicalDisordersCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(45), 82.0);
            Enrollment leeRadiologyEnrollment = createEnrollment(enrollmentRepository, studentLee, diagnosticImagingCourse, EnrollmentStatus.APPROVED, LocalDateTime.now().minusDays(50), 88.0);

            // Create progress tracking
            log.info("Creating progress data...");
            // Track overall course progress
            createProgress(progressRepository, studentSmith, anatomyCourse, 100.0, LocalDateTime.now().minusDays(15));
            createProgress(progressRepository, studentSmith, cardiologyCourse, 65.0, LocalDateTime.now().minusDays(2));
            createProgress(progressRepository, studentBrown, anatomyCourse, 100.0, LocalDateTime.now().minusDays(16));
            createProgress(progressRepository, studentBrown, surgicalTechCourse, 78.0, LocalDateTime.now().minusDays(3));
            
            // Track content-specific progress
            createContentProgress(contentProgressRepository, smithAnatomyEnrollment, skeletalLecture, 100.0, LocalDateTime.now().minusDays(55));
            createContentProgress(contentProgressRepository, smithAnatomyEnrollment, jointLecture, 100.0, LocalDateTime.now().minusDays(50));
            createContentProgress(contentProgressRepository, smithAnatomyEnrollment, boneDevelopment, 100.0, LocalDateTime.now().minusDays(45));
            createContentProgress(contentProgressRepository, smithAnatomyEnrollment, muscleTypes, 100.0, LocalDateTime.now().minusDays(40));
            createContentProgress(contentProgressRepository, smithAnatomyEnrollment, muscleContraction, 100.0, LocalDateTime.now().minusDays(35));
            createContentProgress(contentProgressRepository, smithAnatomyEnrollment, heartAnatomy, 100.0, LocalDateTime.now().minusDays(30));
            createContentProgress(contentProgressRepository, smithAnatomyEnrollment, vesselStructure, 100.0, LocalDateTime.now().minusDays(25));
            createContentProgress(contentProgressRepository, smithAnatomyEnrollment, brainAnatomy, 100.0, LocalDateTime.now().minusDays(20));
            createContentProgress(contentProgressRepository, smithAnatomyEnrollment, spinalCord, 100.0, LocalDateTime.now().minusDays(15));
            
            // Create content progress for Smith in Cardiology
            createContentProgress(contentProgressRepository, smithCardiologyEnrollment, ecgBasics, 100.0, LocalDateTime.now().minusDays(10));
            createContentProgress(contentProgressRepository, smithCardiologyEnrollment, heartFailure, 30.0, LocalDateTime.now().minusDays(2));
            
            // Create content progress for Brown
            createContentProgress(contentProgressRepository, brownAnatomyEnrollment, skeletalLecture, 100.0, LocalDateTime.now().minusDays(53));
            createContentProgress(contentProgressRepository, brownAnatomyEnrollment, jointLecture, 100.0, LocalDateTime.now().minusDays(48));
            createContentProgress(contentProgressRepository, brownAnatomyEnrollment, boneDevelopment, 100.0, LocalDateTime.now().minusDays(43));
            createContentProgress(contentProgressRepository, brownAnatomyEnrollment, muscleTypes, 100.0, LocalDateTime.now().minusDays(38));
            createContentProgress(contentProgressRepository, brownAnatomyEnrollment, muscleContraction, 100.0, LocalDateTime.now().minusDays(33));
            createContentProgress(contentProgressRepository, brownAnatomyEnrollment, heartAnatomy, 100.0, LocalDateTime.now().minusDays(28));
            createContentProgress(contentProgressRepository, brownAnatomyEnrollment, vesselStructure, 100.0, LocalDateTime.now().minusDays(23));
            createContentProgress(contentProgressRepository, brownAnatomyEnrollment, brainAnatomy, 100.0, LocalDateTime.now().minusDays(18));
            createContentProgress(contentProgressRepository, brownAnatomyEnrollment, spinalCord, 100.0, LocalDateTime.now().minusDays(13));

            // Create quizzes and questions
            log.info("Creating assessment items...");
            // Anatomy quiz
            Quiz anatomyQuiz = createQuiz(quizRepository, "Human Anatomy Midterm", "Comprehensive assessment of skeletal and muscular systems", 60, LocalDateTime.now().minusDays(40), LocalDateTime.now().minusDays(39), 70.0, true, true, anatomyCourse);
            
            // Create questions for anatomy quiz
            Question q1 = createQuestion(questionRepository, "Which of the following is NOT a bone in the human wrist?", QuestionType.MULTIPLE_CHOICE, 5, 1, "The human wrist contains 8 carpal bones.", anatomyQuiz);
            createAnswerOption(answerOptionRepository, "Scaphoid", false, "The scaphoid is one of the carpal bones.", 1, q1);
            createAnswerOption(answerOptionRepository, "Lunate", false, "The lunate is one of the carpal bones.", 2, q1);
            createAnswerOption(answerOptionRepository, "Metacarpal", true, "Correct! Metacarpals are the bones in the hand, not the wrist.", 3, q1);
            createAnswerOption(answerOptionRepository, "Pisiform", false, "The pisiform is one of the carpal bones.", 4, q1);
            
            Question q2 = createQuestion(questionRepository, "The femur articulates with which of the following bones?", QuestionType.MULTIPLE_ANSWER, 5, 2, "The femur articulates with the acetabulum of the pelvis and the tibia of the lower leg.", anatomyQuiz);
            createAnswerOption(answerOptionRepository, "Pelvis", true, "Correct! The femur articulates with the acetabulum of the pelvis.", 1, q2);
            createAnswerOption(answerOptionRepository, "Tibia", true, "Correct! The femur articulates with the tibia at the knee joint.", 2, q2);
            createAnswerOption(answerOptionRepository, "Fibula", false, "The fibula does not directly articulate with the femur.", 3, q2);
            createAnswerOption(answerOptionRepository, "Patella", false, "The patella is a sesamoid bone within the quadriceps tendon that articulates with the femur but is not considered a primary articulation.", 4, q2);
            
            Question q3 = createQuestion(questionRepository, "The biceps brachii muscle has its origin on the:", QuestionType.MULTIPLE_CHOICE, 5, 3, "The biceps brachii has two heads with origins on the scapula.", anatomyQuiz);
            createAnswerOption(answerOptionRepository, "Humerus", false, "The biceps brachii originates on the scapula, not the humerus.", 1, q3);
            createAnswerOption(answerOptionRepository, "Scapula", true, "Correct! The biceps brachii has two heads that originate on the scapula.", 2, q3);
            createAnswerOption(answerOptionRepository, "Radius", false, "The radius is where the biceps brachii inserts, not where it originates.", 3, q3);
            createAnswerOption(answerOptionRepository, "Ulna", false, "The biceps brachii does not originate on the ulna.", 4, q3);
            
            Question q4 = createQuestion(questionRepository, "The human heart has how many chambers?", QuestionType.MULTIPLE_CHOICE, 5, 4, "The human heart is a four-chambered organ.", anatomyQuiz);
            createAnswerOption(answerOptionRepository, "2", false, "The human heart has 4 chambers, not 2.", 1, q4);
            createAnswerOption(answerOptionRepository, "3", false, "The human heart has 4 chambers, not 3.", 2, q4);
            createAnswerOption(answerOptionRepository, "4", true, "Correct! The human heart has 4 chambers: right atrium, right ventricle, left atrium, and left ventricle.", 3, q4);
            createAnswerOption(answerOptionRepository, "5", false, "The human heart has 4 chambers, not 5.", 4, q4);
            
            Question q5 = createQuestion(questionRepository, "Describe the structure and function of the blood-brain barrier.", QuestionType.ESSAY, 10, 5, "The blood-brain barrier is a highly selective semipermeable border that separates the circulating blood from the brain and extracellular fluid in the central nervous system.", anatomyQuiz);
            createAnswerOption(answerOptionRepository, "The blood-brain barrier is a highly selective semipermeable border of endothelial cells that prevents solutes in the circulating blood from non-selectively crossing into the extracellular fluid of the central nervous system where neurons reside. It is formed by brain endothelial cells connected by tight junctions and serves to protect the brain from pathogens, toxins, and hormones while allowing essential nutrients to pass through.", true, "Key points to include: selective permeability, endothelial cells, tight junctions, protection of CNS, allowing nutrients to pass.", 1, q5);
            
            // Cardiology quiz
            Quiz cardiologyQuiz = createQuiz(quizRepository, "Cardiac Assessment", "Evaluation of heart function and cardiac disorders", 45, LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(18), 75.0, true, true, cardiologyCourse);
            
            Question cq1 = createQuestion(questionRepository, "The P wave on an ECG represents:", QuestionType.MULTIPLE_CHOICE, 5, 1, "The P wave represents atrial depolarization.", cardiologyQuiz);
            createAnswerOption(answerOptionRepository, "Ventricular depolarization", false, "Ventricular depolarization is represented by the QRS complex.", 1, cq1);
            createAnswerOption(answerOptionRepository, "Atrial depolarization", true, "Correct! The P wave represents atrial depolarization.", 2, cq1);
            createAnswerOption(answerOptionRepository, "Ventricular repolarization", false, "Ventricular repolarization is represented by the T wave.", 3, cq1);
            createAnswerOption(answerOptionRepository, "Atrial repolarization", false, "Atrial repolarization is usually obscured by the QRS complex.", 4, cq1);
            
            Question cq2 = createQuestion(questionRepository, "Which of the following medications is NOT used to treat heart failure?", QuestionType.MULTIPLE_CHOICE, 5, 2, "Common heart failure medications include ACE inhibitors, beta-blockers, diuretics, and ARBs.", cardiologyQuiz);
            createAnswerOption(answerOptionRepository, "ACE inhibitors", false, "ACE inhibitors are commonly used to treat heart failure.", 1, cq2);
            createAnswerOption(answerOptionRepository, "Beta-blockers", false, "Beta-blockers are commonly used to treat heart failure.", 2, cq2);
            createAnswerOption(answerOptionRepository, "Warfarin", true, "Correct! Warfarin is an anticoagulant, not a primary heart failure medication.", 3, cq2);
            createAnswerOption(answerOptionRepository, "Diuretics", false, "Diuretics are commonly used to treat heart failure.", 4, cq2);
            
            // Create quiz attempts
            QuizAttempt smithAnatomyAttempt = createQuizAttempt(quizAttemptRepository, anatomyQuiz, studentSmith, LocalDateTime.now().minusDays(39).plusHours(1), LocalDateTime.now().minusDays(39).plusHours(2), 80.0, 80.0, true, AttemptStatus.COMPLETED);
            QuizAttempt brownAnatomyAttempt = createQuizAttempt(quizAttemptRepository, anatomyQuiz, studentBrown, LocalDateTime.now().minusDays(39).plusHours(2), LocalDateTime.now().minusDays(39).plusHours(3), 85.0, 85.0, true, AttemptStatus.COMPLETED);
            QuizAttempt garciaAnatomyAttempt = createQuizAttempt(quizAttemptRepository, anatomyQuiz, studentGarcia, LocalDateTime.now().minusDays(39).plusHours(3), LocalDateTime.now().minusDays(39).plusHours(4), 65.0, 65.0, false, AttemptStatus.COMPLETED);
            
            // Create student answers
            createStudentAnswer(studentAnswerRepository, smithAnatomyAttempt, q1, List.of(answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(q1.getId()).get(2)), null, 5.0, true, false, null);
            createStudentAnswer(studentAnswerRepository, smithAnatomyAttempt, q2, List.of(answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(q2.getId()).get(0), answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(q2.getId()).get(1)), null, 5.0, true, false, null);
            createStudentAnswer(studentAnswerRepository, smithAnatomyAttempt, q3, List.of(answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(q3.getId()).get(1)), null, 5.0, true, false, null);
            createStudentAnswer(studentAnswerRepository, smithAnatomyAttempt, q4, List.of(answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(q4.getId()).get(2)), null, 5.0, true, false, null);
            createStudentAnswer(studentAnswerRepository, smithAnatomyAttempt, q5, List.of(), "The blood-brain barrier is a protective mechanism that prevents harmful substances from entering the brain while allowing necessary nutrients to pass through. It consists of tightly packed endothelial cells that form the walls of brain capillaries, with tight junctions between them that restrict passage of most molecules.", 8.0, true, true, "Good explanation of the structure and function, but could include more details about the specific transport mechanisms.");
            
            // Create assignments
            Assignment anatomyAssignment = createAssignment(assignmentRepository, anatomyCourse, "Skeletal System Diagram", "Create a detailed diagram of the human skeletal system, labeling all major bones and joints. Include a brief description of the function of each bone group.", LocalDateTime.now().minusDays(50), 100);
            Assignment cardiologyAssignment = createAssignment(assignmentRepository, cardiologyCourse, "ECG Analysis Case Study", "Analyze the provided ECG tracings and identify the cardiac abnormalities present. Provide a clinical interpretation and suggested treatment approach for each case.", LocalDateTime.now().minusDays(15), 100);
            Assignment neurologyAssignment = createAssignment(assignmentRepository, neurologicalDisordersCourse, "Neurological Case Report", "Write a detailed case report on a patient with a neurological disorder of your choice. Include pathophysiology, clinical presentation, diagnostic approach, and treatment plan.", LocalDateTime.now().minusDays(25), 100);
            
            // Create scores for assignments
            createScore(scoreRepository, studentSmith.getId(), anatomyAssignment.getId(), 92, LocalDateTime.now().minusDays(45));
            createScore(scoreRepository, studentBrown.getId(), anatomyAssignment.getId(), 88, LocalDateTime.now().minusDays(44));
            createScore(scoreRepository, studentPatel.getId(), anatomyAssignment.getId(), 95, LocalDateTime.now().minusDays(46));
            createScore(scoreRepository, studentGarcia.getId(), anatomyAssignment.getId(), 78, LocalDateTime.now().minusDays(45));
            createScore(scoreRepository, studentSmith.getId(), cardiologyAssignment.getId(), 85, LocalDateTime.now().minusDays(10));
            createScore(scoreRepository, studentJones.getId(), neurologyAssignment.getId(), 90, LocalDateTime.now().minusDays(20));
            
            // Create submissions
            createSubmission(submissionRepository, anatomyAssignment.getId(), studentSmith.getId(), "smith_anatomy_assignment.pdf", LocalDateTime.now().minusDays(48));
            createSubmission(submissionRepository, anatomyAssignment.getId(), studentBrown.getId(), "brown_anatomy_assignment.pdf", LocalDateTime.now().minusDays(49));
            createSubmission(submissionRepository, cardiologyAssignment.getId(), studentSmith.getId(), "smith_cardiology_assignment.pdf", LocalDateTime.now().minusDays(12));
            createSubmission(submissionRepository, neurologyAssignment.getId(), studentJones.getId(), "jones_neurology_assignment.pdf", LocalDateTime.now().minusDays(22));
            
            // Create forum threads and posts
            log.info("Creating forum discussions...");
            ForumThread anatomyThread = createForumThread(forumThreadRepository, anatomyCourse.getId(), "Difficulty understanding joint classifications", studentSmith.getId(), LocalDateTime.now().minusDays(52));
            ForumThread cardiologyThread = createForumThread(forumThreadRepository, cardiologyCourse.getId(), "Question about heart valve sounds", studentGarcia.getId(), LocalDateTime.now().minusDays(25));
            ForumThread pediatricsThread = createForumThread(forumThreadRepository, pediatricCareCourse.getId(), "Developmental milestones reference", studentWilson.getId(), LocalDateTime.now().minusDays(35));
            
            // Create forum posts
            createForumPost(forumPostRepository, anatomyThread.getId(), studentSmith.getId(), "I'm having trouble understanding the classification of joints, particularly the difference between functional and structural classifications. Can someone explain this in simpler terms?", LocalDateTime.now().minusDays(52));
            createForumPost(forumPostRepository, anatomyThread.getId(), instructorJohnson.getId(), "Great question! Structural classification refers to how the joints are physically connected (fibrous, cartilaginous, synovial), while functional classification refers to how much movement they allow (synarthrosis, amphiarthrosis, diarthrosis). For example, a suture in the skull is structurally fibrous and functionally a synarthrosis (immovable).", LocalDateTime.now().minusDays(51));
            createForumPost(forumPostRepository, anatomyThread.getId(), studentBrown.getId(), "I found this helpful diagram showing examples of each type: [link to diagram]. It really helped me understand the differences.", LocalDateTime.now().minusDays(50));
            createForumPost(forumPostRepository, anatomyThread.getId(), studentSmith.getId(), "Thank you both! That clarifies it for me. @Dr. Johnson, do we need to know specific examples of each type for the exam?", LocalDateTime.now().minusDays(49));
            createForumPost(forumPostRepository, anatomyThread.getId(), instructorJohnson.getId(), "Yes, you should be familiar with common examples of each type. Review the examples in Chapter 8 and the lab slides from week 3.", LocalDateTime.now().minusDays(48));
            
            createForumPost(forumPostRepository, cardiologyThread.getId(), studentGarcia.getId(), "I'm confused about the difference between S1 and S2 heart sounds. How can you distinguish them when auscultating?", LocalDateTime.now().minusDays(25));
            createForumPost(forumPostRepository, cardiologyThread.getId(), instructorJohnson.getId(), "S1 ('lub') is caused by closure of the mitral and tricuspid valves at the beginning of systole, while S2 ('dub') is caused by closure of the aortic and pulmonary valves at the beginning of diastole. S1 is usually louder at the apex of the heart, while S2 is louder at the base. Also, S1 is generally longer and lower pitched than S2.", LocalDateTime.now().minusDays(24));
            
            createForumPost(forumPostRepository, pediatricsThread.getId(), studentWilson.getId(), "Does anyone have a good reference chart for developmental milestones? I'm finding conflicting information in different sources.", LocalDateTime.now().minusDays(35));
            createForumPost(forumPostRepository, pediatricsThread.getId(), instructorRodriguez.getId(), "The CDC has an excellent reference guide that we use in clinical practice. I'll upload it to the course resources section. Remember that these are guidelines and there's normal variation in development.", LocalDateTime.now().minusDays(34));
            createForumPost(forumPostRepository, pediatricsThread.getId(), studentPatel.getId(), "I've found the WHO growth charts to be reliable as well, especially for global comparisons.", LocalDateTime.now().minusDays(33));
            
            // Create notifications
            log.info("Creating notifications...");
            createNotification(notificationRepository, NotificationType.COURSE_CONTENT_UPLOAD, studentSmith, "New Content: Heart Anatomy", "New content has been added to your course: Cardiovascular Medicine", cardiologyCourse.getId(), "course");
            createNotification(notificationRepository, NotificationType.ASSIGNMENT_DEADLINE_24H, studentSmith, "Assignment Due in 24 Hours", "Your assignment 'ECG Analysis Case Study' is due soon.", cardiologyAssignment.getId(), "assignment");
            createNotification(notificationRepository, NotificationType.QUIZ_AVAILABLE, studentBrown, "New Quiz Available: Human Anatomy Midterm", "A new quiz is available in your course: Human Anatomy Fundamentals", anatomyQuiz.getId(), "quiz");
            createNotification(notificationRepository, NotificationType.FORUM_REPLY, studentGarcia, "New Reply to Your Forum Post", "Dr. Sarah Johnson replied to your post: \"I'm confused about the difference between S1 and S2 heart sounds...\"", cardiologyThread.getId(), "forumPost");
            createNotification(notificationRepository, NotificationType.GRADE_POSTED, studentSmith, "Grade Posted: Skeletal System Diagram", "Your grade for Skeletal System Diagram in Human Anatomy Fundamentals has been posted.", anatomyAssignment.getId(), "assignment");
            
            // Create notification preferences
            createNotificationPreference(notificationPreferenceRepository, studentSmith, NotificationType.COURSE_CONTENT_UPLOAD, true, true);
            createNotificationPreference(notificationPreferenceRepository, studentSmith, NotificationType.ASSIGNMENT_DEADLINE_24H, true, true);
            createNotificationPreference(notificationPreferenceRepository, studentSmith, NotificationType.QUIZ_AVAILABLE, true, true);
            createNotificationPreference(notificationPreferenceRepository, studentSmith, NotificationType.FORUM_REPLY, true, true);
            createNotificationPreference(notificationPreferenceRepository, studentSmith, NotificationType.GRADE_POSTED, true, true);
            
            // Create certificates
            log.info("Creating certificates...");
            createCertificate(certificateRepository, studentSmith.getId(), anatomyCourse.getId(), "certificate_smith_anatomy.pdf", LocalDateTime.now().minusDays(14), "Human Anatomy Fundamentals");
            createCertificate(certificateRepository, studentBrown.getId(), anatomyCourse.getId(), "certificate_brown_anatomy.pdf", LocalDateTime.now().minusDays(12), "Human Anatomy Fundamentals");
            createCertificate(certificateRepository, studentPatel.getId(), anatomyCourse.getId(), "certificate_patel_anatomy.pdf", LocalDateTime.now().minusDays(11), "Human Anatomy Fundamentals");
            
            log.info("Database initialized with healthcare LMS test data!");
        };
    }
    
    // Helper methods to create entities
    
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
    
    private Permission createPermission(PermissionRepository repository, String name, String description) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(description);
        permission.setActive(true);
        return repository.save(permission);
    }
    
    private Role createRole(RoleRepository repository, String name, String description) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setActive(true);
        role.setPermissions(new HashSet<>());
        return repository.save(role);
    }
    
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
    
    private Module createModule(ModuleRepository repository, String title, String description, Integer sequence) {
        Module module = new Module();
        module.setTitle(title);
        module.setDescription(description);
        module.setSequence(sequence);
        return repository.save(module);
    }
    
    private Tag createTag(TagRepository repository, String name) {
        Tag tag = new Tag();
        tag.setName(name);
        return repository.save(tag);
    }
    
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
    
    private Progress createProgress(ProgressRepository repository, User student, Course course, Double progress, LocalDateTime lastUpdated) {
        Progress progressEntity = new Progress(student, course, progress, lastUpdated);
        return repository.save(progressEntity);
    }
    
    private ContentProgress createContentProgress(ContentProgressRepository repository, Enrollment enrollment, Content content, Double progress, LocalDateTime lastUpdated) {
        ContentProgress contentProgress = ContentProgress.builder()
                .enrollment(enrollment)
                .content(content)
                .progress(progress)
                .lastUpdated(lastUpdated)
                .build();
        return repository.save(contentProgress);
    }
    
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
    
    private Assignment createAssignment(AssignmentRepository repository, Course course, String title, String description, LocalDateTime dueDate, Integer maxScore) {
        Assignment assignment = new Assignment();
        assignment.setCourseId(course.getId());
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setDueDate(dueDate);
        assignment.setMaxScore(maxScore);
        return repository.save(assignment);
    }
    
    private Score createScore(ScoreRepository repository, Long studentId, Long assignmentId, Integer score, LocalDateTime gradedDate) {
        Score scoreEntity = new Score();
        scoreEntity.setStudentId(studentId);
        scoreEntity.setAssignmentId(assignmentId);
        scoreEntity.setScore(score);
        scoreEntity.setGradedDate(gradedDate);
        return repository.save(scoreEntity);
    }
    
    private Submission createSubmission(SubmissionRepository repository, Long assignmentId, Long studentId, String filePath, LocalDateTime submissionDate) {
        Submission submission = new Submission();
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setFilePath(filePath);
        submission.setSubmissionDate(submissionDate);
        return repository.save(submission);
    }
    
    private ForumThread createForumThread(ForumThreadRepository repository, Long courseId, String title, Long createdBy, LocalDateTime createdDate) {
        ForumThread thread = new ForumThread();
        thread.setCourseId(courseId);
        thread.setTitle(title);
        thread.setCreatedBy(createdBy);
        thread.setCreatedDate(createdDate);
        return repository.save(thread);
    }
    
    private ForumPost createForumPost(ForumPostRepository repository, Long threadId, Long userId, String content, LocalDateTime postedDate) {
        ForumPost post = new ForumPost();
        post.setThreadId(threadId);
        post.setUserId(userId);
        post.setContent(content);
        post.setPostedDate(postedDate);
        return repository.save(post);
    }
    
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