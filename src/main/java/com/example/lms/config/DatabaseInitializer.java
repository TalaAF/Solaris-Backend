// package com.example.lms.config;

// import com.example.lms.Department.model.Department;
// import com.example.lms.Department.repository.DepartmentRepository;
// import com.example.lms.assessment.model.*;
// import com.example.lms.assessment.repository.*;
// import com.example.lms.assignment.assignments.model.Assignment;
// import com.example.lms.assignment.assignments.model.Score;
// import com.example.lms.assignment.assignments.repository.AssignmentRepository;
// import com.example.lms.assignment.assignments.repository.ScoreRepository;
// import com.example.lms.assignment.forum.model.ForumPost;
// import com.example.lms.assignment.forum.model.ForumThread;
// import com.example.lms.assignment.forum.repository.ForumPostRepository;
// import com.example.lms.assignment.forum.repository.ForumThreadRepository;
// import com.example.lms.assignment.submission.model.Submission;
// import com.example.lms.assignment.submission.repository.SubmissionRepository;
// import com.example.lms.certificate.model.Certificate;
// import com.example.lms.certificate.model.CertificateTemplate;
// import com.example.lms.certificate.repository.CertificateRepository;
// import com.example.lms.certificate.repository.CertificateTemplateRepository;
// import com.example.lms.content.model.*;
// import org.springframework.transaction.annotation.Propagation;
// import org.springframework.transaction.annotation.Transactional;
// import com.example.lms.content.model.Module;
// import com.example.lms.content.repository.*;
// import com.example.lms.course.model.Course;
// import com.example.lms.course.repository.CourseRepository;
// import com.example.lms.enrollment.model.Enrollment;
// import com.example.lms.enrollment.model.EnrollmentStatus;
// import com.example.lms.enrollment.repository.EnrollmentRepository;
// import com.example.lms.notification.model.Notification;
// import com.example.lms.notification.model.NotificationPreference;
// import com.example.lms.notification.model.NotificationType;
// import com.example.lms.notification.repository.NotificationPreferenceRepository;
// import com.example.lms.notification.repository.NotificationRepository;
// import com.example.lms.progress.model.ContentProgress;
// import com.example.lms.progress.model.Progress;
// import com.example.lms.progress.repository.ContentProgressRepository;
// import com.example.lms.progress.repository.ProgressRepository;
// import com.example.lms.security.model.Permission;
// import com.example.lms.security.model.Role;
// import com.example.lms.security.repository.PermissionRepository;
// import com.example.lms.security.repository.RoleRepository;
// import com.example.lms.user.model.User;
// import com.example.lms.user.model.UserProfile;
// import com.example.lms.user.repository.UserProfileRepository;
// import com.example.lms.user.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Profile;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.*;
// import java.util.stream.Collectors;

// /**
//  * Healthcare LMS Database Initializer
//  * Populates the database with realistic medical education dummy data
//  * for testing and development purposes.
//  */
// @Configuration
// @RequiredArgsConstructor
// @Slf4j
// @Profile("!prod") // Only run in non-production environments
// public class DatabaseInitializer {

//     private final PasswordEncoder passwordEncoder;

//     @Value("${app.database.initialize:true}")
//     private boolean initializeDatabase;

//     @Bean
//     public CommandLineRunner initDatabase(
//             DepartmentRepository departmentRepository,
//             UserRepository userRepository,
//             UserProfileRepository userProfileRepository,
//             RoleRepository roleRepository,
//             PermissionRepository permissionRepository,
//             CourseRepository courseRepository,
//             EnrollmentRepository enrollmentRepository,
//             ContentRepository contentRepository,
//             ModuleRepository moduleRepository,
//             TagRepository tagRepository,
//             ContentProgressRepository contentProgressRepository,
//             ProgressRepository progressRepository,
//             CertificateRepository certificateRepository,
//             CertificateTemplateRepository certificateTemplateRepository,
//             QuizRepository quizRepository,
//             QuestionRepository questionRepository,
//             AnswerOptionRepository answerOptionRepository,
//             QuizAttemptRepository quizAttemptRepository,
//             StudentAnswerRepository studentAnswerRepository,
//             AssignmentRepository assignmentRepository,
//             ScoreRepository scoreRepository,
//             SubmissionRepository submissionRepository,
//             ForumThreadRepository forumThreadRepository,
//             ForumPostRepository forumPostRepository,
//             NotificationRepository notificationRepository,
//             NotificationPreferenceRepository notificationPreferenceRepository
//     ) {
//         return args -> {
//             if (!initializeDatabase) {
//                 log.info("Database initialization is disabled via configuration.");
//                 return;
//             }

//             try {
//                 log.info("Loading Healthcare LMS database with test data...");
//                 // Check if data already exists
//                 if (userRepository.count() > 0) {
//                     log.info("Database already contains data, skipping initialization.");
//                     return;
//                 }

//                 // Create departments with its own transaction
//                 Map<String, Department> departments = initDepartments(departmentRepository);
                
//                 // Create roles and permissions
//                 Map<String, Role> roles = initRolesAndPermissions(roleRepository, permissionRepository);
                
//                 // Create users
//                 Map<String, User> users = initUsers(userRepository, roles, departments);
                
//                 // Create user profiles
//                 initUserProfiles(userProfileRepository, users);
                
//                 // Create courses
//                 Map<String, Course> courses = initCourses(courseRepository, users, departments, userRepository, roles);
                
//                 // Create modules and tags
//                 Map<String, Module> modules = initModules(moduleRepository, courses);
//                 Map<String, Tag> tags = initTags(tagRepository);
                
//                 // Create content
//                 Map<String, Content> contents = initContent(contentRepository, courses, modules, tags);
                
//                 // Create enrollments
//                 Map<String, Enrollment> enrollments = initEnrollments(enrollmentRepository, users, courses);
                
//                 // Create progress data
//                 initProgress(progressRepository, users, courses);
//                 initContentProgress(contentProgressRepository, enrollments, contents);
                
//                 // Create certificate templates
//                 Map<String, CertificateTemplate> templates = initCertificateTemplates(
//                     certificateTemplateRepository, courses, departments);
                
//                 // Create certificates
//                 initCertificates(certificateRepository, users, courses, templates);
                
//                 // Create assessments
//                 Map<String, Quiz> quizzes = initQuizzes(quizRepository, courses);
//                 Map<String, Question> questions = initQuestions(questionRepository, quizzes);
//                 initAnswerOptions(answerOptionRepository, questions);
                
//                 // Create quiz attempts
//                 Map<String, QuizAttempt> attempts = initQuizAttempts(quizAttemptRepository, quizzes, users);
//                 initStudentAnswers(studentAnswerRepository, attempts, questions, answerOptionRepository);
                
//                 // Create assignments
//                 Map<String, Assignment> assignments = initAssignments(assignmentRepository, courses, users);
//                 initScores(scoreRepository, users, assignments);
//                 initSubmissions(submissionRepository, assignments, users);
                
//                 // Create forum discussions
//                 Map<String, ForumThread> threads = initForumThreads(forumThreadRepository, courses, users);
//                 initForumPosts(forumPostRepository, threads, users);
                
//                 // Create notifications
//                 initNotifications(notificationRepository, users, courses, quizzes, assignments, threads);
//                 initNotificationPreferences(notificationPreferenceRepository, users);
                
//                 log.info("Database initialized with healthcare LMS test data aligned with frontend!");
//             } catch (Exception e) {
//                 log.error("Failed to initialize database with test data", e);
//                 e.printStackTrace();
//                 throw e;
//             }
//         };
//     }
    
//     @Transactional(propagation = Propagation.REQUIRES_NEW)
//     private Map<String, CertificateTemplate> initCertificateTemplates(
//             CertificateTemplateRepository repository, 
//             Map<String, Course> courses,
//             Map<String, Department> departments) {
        
//         log.info("Creating certificate templates...");
//         Map<String, CertificateTemplate> templates = new HashMap<>();
        
//         try {
//             // Standard Template for Anatomy
//             CertificateTemplate anatomyTemplate = new CertificateTemplate();
//             anatomyTemplate.setName("Standard Certificate");
//             anatomyTemplate.setDescription("Basic completion certificate template");
//             anatomyTemplate.setCourseId(courses.get("anatomyPhysiology").getId());
//             anatomyTemplate.setCourseName(courses.get("anatomyPhysiology").getTitle());
//             anatomyTemplate.setDepartmentId(departments.get("anatomy").getId());
//             anatomyTemplate.setDepartmentName(departments.get("anatomy").getName());
//             anatomyTemplate.setTemplateContent("<div class='certificate'>{{courseName}} - Completion Certificate for {{studentName}}</div>");
//             anatomyTemplate.setActive(true);
//             anatomyTemplate.setIssuedCount(15);
//             anatomyTemplate = repository.save(anatomyTemplate);
//             templates.put("anatomyTemplate", anatomyTemplate);
            
//             // Honors Template for Biochemistry
//             CertificateTemplate biochemTemplate = new CertificateTemplate();
//             biochemTemplate.setName("Honors Certificate");
//             biochemTemplate.setDescription("Premium certificate for high-achieving students");
//             biochemTemplate.setCourseId(courses.get("biochemistry").getId());
//             biochemTemplate.setCourseName(courses.get("biochemistry").getTitle());
//             biochemTemplate.setDepartmentId(departments.get("biochemistry").getId());
//             biochemTemplate.setDepartmentName(departments.get("biochemistry").getName());
//             biochemTemplate.setTemplateContent("<div class='certificate honors'>{{courseName}} - Honors Certificate for {{studentName}}</div>");
//             biochemTemplate.setActive(true);
//             biochemTemplate.setIssuedCount(5);
//             biochemTemplate = repository.save(biochemTemplate);
//             templates.put("biochemTemplate", biochemTemplate);
            
//             // General Achievement Certificate
//             CertificateTemplate generalTemplate = new CertificateTemplate();
//             generalTemplate.setName("General Achievement Certificate");
//             generalTemplate.setDescription("Generic certificate of achievement that can be issued without a specific course");
//             generalTemplate.setDepartmentId(departments.get("medFoundation").getId());
//             generalTemplate.setDepartmentName(departments.get("medFoundation").getName());
//             generalTemplate.setTemplateContent("<div class='certificate general'>Certificate of Achievement for {{studentName}}</div>");
//             generalTemplate.setActive(true);
//             generalTemplate.setIssuedCount(3);
//             generalTemplate = repository.save(generalTemplate);
//             templates.put("generalTemplate", generalTemplate);
            
//             log.info("Successfully created {} certificate templates", templates.size());
//         } catch (Exception e) {
//             log.error("Error creating certificate templates: " + e.getMessage(), e);
//         }
        
//         return templates;
//     }

//     @Transactional(propagation = Propagation.REQUIRES_NEW)
//     private void initCertificates(
//             CertificateRepository certificateRepository, 
//             Map<String, User> users, 
//             Map<String, Course> courses,
//             Map<String, CertificateTemplate> templates) {
            
//         log.info("Creating certificates...");
        
//         try {
//             // Generate verification IDs
//             String verificationId1 = UUID.randomUUID().toString().substring(0, 8);
//             String verificationId2 = UUID.randomUUID().toString().substring(0, 8);
            
//             // Create certificate for John Doe in Anatomy & Physiology
//             if (users.get("studentJohn") != null && courses.get("anatomyPhysiology") != null) {
//                 Certificate certificate = Certificate.builder()
//                     .studentId(users.get("studentJohn").getId())
//                     .courseId(courses.get("anatomyPhysiology").getId())
//                     .courseName("Human Anatomy & Physiology")
//                     .certificateUrl("certificate_john_anatomy.pdf")
//                     .verificationId("cert-" + verificationId1)
//                     .issuedAt(LocalDateTime.now().minusDays(14))
//                     .revoked(false)
//                     .build();
                
//                 // Add template details if available
//                 if (templates.get("anatomyTemplate") != null) {
//                     certificate.setTemplate(templates.get("anatomyTemplate").getName());
//                 }
                
//                 certificateRepository.save(certificate);
//             }
            
//             // Create certificate for Jane in Biochemistry
//             if (users.get("studentJane") != null && courses.get("biochemistry") != null) {
//                 Certificate certificate = Certificate.builder()
//                     .studentId(users.get("studentJane").getId())
//                     .courseId(courses.get("biochemistry").getId())
//                     .courseName("Biochemistry Fundamentals")
//                     .certificateUrl("certificate_jane_biochem.pdf")
//                     .verificationId("cert-" + verificationId2)
//                     .issuedAt(LocalDateTime.now().minusDays(12))
//                     .revoked(false)
//                     .template(templates.get("biochemTemplate") != null ? 
//                         templates.get("biochemTemplate").getName() : "Default Template")
//                     .build();
                
//                 certificateRepository.save(certificate);
//             }
            
//             log.info("Successfully created certificates");
//         } catch (Exception e) {
//             log.error("Error creating certificates: " + e.getMessage(), e);
//         }
//     }

//     @Transactional(propagation = Propagation.REQUIRES_NEW)
//     private Map<String, Enrollment> initEnrollments(
//             EnrollmentRepository enrollmentRepository, 
//             Map<String, User> users, 
//             Map<String, Course> courses) {
            
//         log.info("Creating enrollments aligned with frontend...");
//         Map<String, Enrollment> enrollments = new HashMap<>();
        
//         try {
//             // Check if both the user and course exist before creating the enrollment
//             User studentJohn = users.get("studentJohn");
//             Course anatomyPhysiology = courses.get("anatomyPhysiology");
            
//             if (studentJohn != null && anatomyPhysiology != null) {
//                 Enrollment johnAnatomy = Enrollment.builder()
//                     .student(studentJohn)
//                     .course(anatomyPhysiology)
//                     .status(EnrollmentStatus.APPROVED) // Maps to "active" in frontend
//                     .enrollmentDate(LocalDateTime.now().minusDays(30))
//                     .lastAccessedDate(LocalDateTime.now().minusDays(2))
//                     .progress(65.0)
//                     .build();
                
//                 johnAnatomy = enrollmentRepository.save(johnAnatomy);
//                 enrollments.put("johnAnatomy", johnAnatomy);
//             }
            
//             User johnForBiochem = users.get("studentJohn");
//             Course biochemistry = courses.get("biochemistry");
            
//             if (johnForBiochem != null && biochemistry != null) {
//                 Enrollment johnBiochem = Enrollment.builder()
//                     .student(johnForBiochem)
//                     .course(biochemistry)
//                     .status(EnrollmentStatus.APPROVED)
//                     .enrollmentDate(LocalDateTime.now().minusDays(28))
//                     .lastAccessedDate(LocalDateTime.now().minusDays(1))
//                     .progress(80.0)
//                     .build();
                
//                 johnBiochem = enrollmentRepository.save(johnBiochem);
//                 enrollments.put("johnBiochem", johnBiochem);
//             }
            
//             // Add a CANCELLED enrollment to test status mapping to "inactive"
//             if (users.get("studentJane") != null && courses.get("anatomyPhysiology") != null) {
//                 Enrollment janeAnatomy = Enrollment.builder()
//                     .student(users.get("studentJane"))
//                     .course(courses.get("anatomyPhysiology"))
//                     .status(EnrollmentStatus.CANCELLED) // This should map to "inactive" in frontend
//                     .enrollmentDate(LocalDateTime.now().minusDays(40))
//                     .lastAccessedDate(LocalDateTime.now().minusDays(35))
//                     .progress(15.0)
//                     .build();
                
//                 janeAnatomy = enrollmentRepository.save(janeAnatomy);
//                 enrollments.put("janeAnatomy", janeAnatomy);
//             }
            
//             log.info("Successfully created {} enrollments", enrollments.size());
//         } catch (Exception e) {
//             log.error("Error creating enrollments: " + e.getMessage(), e);
//         }
        
//         return enrollments;
//     }

//     @Transactional(propagation = Propagation.REQUIRES_NEW)
//     private Map<String, Role> initRolesAndPermissions(RoleRepository roleRepository, PermissionRepository permissionRepository) {
//         // Use the existing createRolesAndPermissions method
//         return createRolesAndPermissions(roleRepository, permissionRepository);
//     }
    
//     @Transactional(propagation = Propagation.REQUIRES_NEW)
//     private Map<String, User> initUsers(UserRepository userRepository, Map<String, Role> roles, Map<String, Department> departments) {
//         log.info("Creating users from frontend mock data...");
//         Map<String, User> users = new HashMap<>();
        
//         try {
//             // Admin user
//             User adminUser = User.builder()
//                 .email("admin@solaris.edu")
//                 .fullName("Admin User")
//                 .password(passwordEncoder.encode("password123"))
//                 .isActive(true)
//                 .tokenVersion(0L)
//                 .department(departments.get("administration"))
//                 .roles(Set.of(roles.get("ADMIN")))
//                 .build();
            
//             adminUser = userRepository.save(adminUser);
//             users.put("admin", adminUser);
            
//             // Instructors
//             User janeSmith = User.builder()
//                 .email("professor@solaris.edu")
//                 .fullName("Jane Smith")
//                 .password(passwordEncoder.encode("password123"))
//                 .isActive(true)
//                 .tokenVersion(0L)
//                 .department(departments.get("mathematics"))
//                 .roles(Set.of(roles.get("INSTRUCTOR")))
//                 .build();
            
//             janeSmith = userRepository.save(janeSmith);
//             users.put("janeSmith", janeSmith);
            
//             User michaelChen = User.builder()
//                 .email("instructor@solaris.edu")
//                 .fullName("Michael Chen")
//                 .password(passwordEncoder.encode("password123"))
//                 .isActive(true)
//                 .tokenVersion(0L)
//                 .department(departments.get("physics"))
//                 .roles(Set.of(roles.get("INSTRUCTOR")))
//                 .build();
            
//             michaelChen = userRepository.save(michaelChen);
//             users.put("michaelChen", michaelChen);
            
//             // Students - these match the frontend mock data
//             User studentAlex = User.builder()
//                 .email("student@solaris.edu")
//                 .fullName("Alex Johnson")
//                 .password(passwordEncoder.encode("password123"))
//                 .isActive(true)
//                 .tokenVersion(0L)
//                 .department(departments.get("computerScience"))
//                 .roles(Set.of(roles.get("STUDENT")))
//                 .build();
            
//             studentAlex = userRepository.save(studentAlex);
//             users.put("studentAlex", studentAlex);
//             users.put("studentJohn", studentAlex); // Alias for compatibility
            
//             User studentSarah = User.builder()
//                 .email("student2@solaris.edu")
//                 .fullName("Sarah Williams")
//                 .password(passwordEncoder.encode("password123"))
//                 .isActive(false) // Inactive as per mock data
//                 .tokenVersion(0L)
//                 .department(departments.get("computerScience"))
//                 .roles(Set.of(roles.get("STUDENT")))
//                 .build();
            
//             studentSarah = userRepository.save(studentSarah);
//             users.put("studentSarah", studentSarah);
//             users.put("studentJane", studentSarah); // Alias for compatibility
            
//             log.info("Successfully created {} users from frontend mock data", users.size());
//         } catch (Exception e) {
//             log.error("Error creating users: " + e.getMessage(), e);
//         }
        
//         return users;
//     }
    
//     @Transactional(propagation = Propagation.REQUIRES_NEW)
//     private void initUserProfiles(UserProfileRepository userProfileRepository, Map<String, User> users) {
//         log.info("Creating user profiles...");
        
//         try {
//             // Create profiles matching frontend mock data
//             createUserProfile(userProfileRepository, users.get("admin"), "Admin", "User", "123-456-7890", "System administrator with full access.");
//             createUserProfile(userProfileRepository, users.get("janeSmith"), "Jane", "Smith", "123-555-0001", "Professor with expertise in mathematics and computer science.");
//             createUserProfile(userProfileRepository, users.get("michaelChen"), "Michael", "Chen", "123-555-0002", "Instructor specializing in physics and advanced computing concepts.");
//             createUserProfile(userProfileRepository, users.get("studentJohn"), "Alex", "Johnson", "123-555-0003", "Computer Science student interested in software development.");
//             createUserProfile(userProfileRepository, users.get("studentJane"), "Sarah", "Williams", "123-555-0004", "Computer Science student focusing on data science.");
            
//             log.info("Successfully created user profiles");
//         } catch (Exception e) {
//             log.error("Error creating user profiles: " + e.getMessage(), e);
//         }
//     }

//     @Transactional(propagation = Propagation.REQUIRES_NEW)
//     private Map<String, Course> initCourses(CourseRepository courseRepository, Map<String, User> users, 
//                                 Map<String, Department> departments, UserRepository userRepository, 
//                                 Map<String, Role> roles) {
//     log.info("Creating courses aligned with frontend mock data...");
//     Map<String, Course> courses = new HashMap<>();
    
//     try {
//         // Courses from frontend mock data
//         Course introCS = createCourse(
//             courseRepository,
//             "Introduction to Computer Science",
//             "An introductory course covering basic concepts in computer science.",
//             users.get("janeSmith"),
//             departments.get("computerScience"),
//             50
//         );
//         introCS.setStartDate(LocalDateTime.now().minusDays(15)); // Jan 15, 2025 in mock data
//         introCS.setEndDate(LocalDateTime.now().plusMonths(4));   // May 30, 2025 in mock data
//         introCS.setActive(true);
//         courseRepository.save(introCS);
//         courses.put("introCS", introCS);
//         courses.put("anatomyPhysiology", introCS); // Alias for compatibility
        
//         Course webDev = createCourse(
//             courseRepository,
//             "Advanced Web Development",
//             "Advanced techniques in web development including modern frameworks.",
//             users.get("janeSmith"),
//             departments.get("computerScience"),
//             30
//         );
//         webDev.setStartDate(LocalDateTime.now().minusDays(14)); // Jan 16, 2025 in mock data
//         webDev.setEndDate(LocalDateTime.now().plusMonths(4));   // May 28, 2025 in mock data
//         webDev.setActive(true);
//         courseRepository.save(webDev);
//         courses.put("webDev", webDev);
        
//         Course dataStructures = createCourse(
//             courseRepository,
//             "Data Structures and Algorithms",
//             "Comprehensive study of data structures and algorithmic techniques.",
//             users.get("michaelChen"),
//             departments.get("computerScience"),
//             40
//         );
//         dataStructures.setStartDate(LocalDateTime.now().minusDays(12)); // Jan 18, 2025 in mock data
//         dataStructures.setEndDate(LocalDateTime.now().plusMonths(4));   // May 29, 2025 in mock data
//         dataStructures.setActive(true);
//         courseRepository.save(dataStructures);
//         courses.put("dataStructures", dataStructures);
//         courses.put("biochemistry", dataStructures); // Alias for compatibility
        
//         // Set prerequisites as in mock data
//         if (courses.get("webDev") != null && courses.get("introCS") != null) {
//             Set<Course> prerequisites = new HashSet<>();
//             prerequisites.add(courses.get("introCS"));
//             courses.get("webDev").setPrerequisites(prerequisites);
//             courseRepository.save(courses.get("webDev"));
//         }
        
//         if (courses.get("dataStructures") != null && courses.get("introCS") != null) {
//             Set<Course> prerequisites = new HashSet<>();
//             prerequisites.add(courses.get("introCS"));
//             courses.get("dataStructures").setPrerequisites(prerequisites);
//             courseRepository.save(courses.get("dataStructures"));
//         }
        
//         log.info("Successfully created {} courses", courses.size());
//     } catch (Exception e) {
//         log.error("Error creating courses: " + e.getMessage(), e);
//     }
    
//     return courses;
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private Map<String, Module> initModules(ModuleRepository moduleRepository, Map<String, Course> courses) {
//     log.info("Creating course modules...");
//     Map<String, Module> modules = new HashMap<>();
    
//     try {
//         // Create modules for Introduction to CS
//         if (courses.get("introCS") != null) {
//             Module introModule1 = createModule(moduleRepository, "Week 1: Introduction to Computing", 
//                 "Fundamental computing concepts and history of computer science", 1);
//             introModule1.setCourse(courses.get("introCS"));
//             moduleRepository.save(introModule1);
//             modules.put("introModule1", introModule1);
            
//             Module introModule2 = createModule(moduleRepository, "Week 2: Programming Basics", 
//                 "Introduction to algorithms and basic programming concepts", 2);
//             introModule2.setCourse(courses.get("introCS"));
//             moduleRepository.save(introModule2);
//             modules.put("introModule2", introModule2);
            
//             Module introModule3 = createModule(moduleRepository, "Week 3: Data Types and Variables", 
//                 "Understanding data types, variables, and memory allocation", 3);
//             introModule3.setCourse(courses.get("introCS"));
//             moduleRepository.save(introModule3);
//             modules.put("introModule3", introModule3);
//         }
        
//         // Create modules for Web Development
//         if (courses.get("webDev") != null) {
//             Module webModule1 = createModule(moduleRepository, "HTML & CSS Fundamentals", 
//                 "Core concepts of HTML structure and CSS styling", 1);
//             webModule1.setCourse(courses.get("webDev"));
//             moduleRepository.save(webModule1);
//             modules.put("webModule1", webModule1);
            
//             Module webModule2 = createModule(moduleRepository, "JavaScript Essentials", 
//                 "JavaScript syntax, DOM manipulation, and event handling", 2);
//             webModule2.setCourse(courses.get("webDev"));
//             moduleRepository.save(webModule2);
//             modules.put("webModule2", webModule2);
            
//             Module webModule3 = createModule(moduleRepository, "Frontend Frameworks", 
//                 "Introduction to modern frontend frameworks like React", 3);
//             webModule3.setCourse(courses.get("webDev"));
//             moduleRepository.save(webModule3);
//             modules.put("webModule3", webModule3);
//         }
        
//         // Create modules for Data Structures
//         if (courses.get("dataStructures") != null) {
//             Module dsModule1 = createModule(moduleRepository, "Arrays and Linked Lists", 
//                 "Understanding basic linear data structures", 1);
//             dsModule1.setCourse(courses.get("dataStructures"));
//             moduleRepository.save(dsModule1);
//             modules.put("dsModule1", dsModule1);
            
//             Module dsModule2 = createModule(moduleRepository, "Stacks and Queues", 
//                 "LIFO and FIFO data structures and their applications", 2);
//             dsModule2.setCourse(courses.get("dataStructures"));
//             moduleRepository.save(dsModule2);
//             modules.put("dsModule2", dsModule2);
            
//             Module dsModule3 = createModule(moduleRepository, "Trees and Graphs", 
//                 "Hierarchical and network data structures", 3);
//             dsModule3.setCourse(courses.get("dataStructures"));
//             moduleRepository.save(dsModule3);
//             modules.put("dsModule3", dsModule3);
//         }
        
//         log.info("Successfully created {} modules", modules.size());
//     } catch (Exception e) {
//         log.error("Error creating modules: " + e.getMessage(), e);
//     }
    
//     return modules;
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private Map<String, Tag> initTags(TagRepository tagRepository) {
//     log.info("Creating content tags...");
//     Map<String, Tag> tags = new HashMap<>();
    
//     try {
//         // Create generic tags for difficulty levels
//         Tag beginnerTag = createTag(tagRepository, "beginner");
//         tags.put("beginner", beginnerTag);
        
//         Tag intermediateTag = createTag(tagRepository, "intermediate");
//         tags.put("intermediate", intermediateTag);
        
//         Tag advancedTag = createTag(tagRepository, "advanced");
//         tags.put("advanced", advancedTag);
        
//         // Create subject-specific tags
//         Tag programmingTag = createTag(tagRepository, "programming");
//         tags.put("programming", programmingTag);
        
//         Tag webdevTag = createTag(tagRepository, "webdev");
//         tags.put("webdev", webdevTag);
        
//         Tag javascriptTag = createTag(tagRepository, "javascript");
//         tags.put("javascript", javascriptTag);
        
//         Tag htmlCssTag = createTag(tagRepository, "html-css");
//         tags.put("html-css", htmlCssTag);
        
//         Tag algorithmsTag = createTag(tagRepository, "algorithms");
//         tags.put("algorithms", algorithmsTag);
        
//         Tag datastructuresTag = createTag(tagRepository, "data-structures");
//         tags.put("datastructures", datastructuresTag);
        
//         // Content type tags
//         Tag lectureTag = createTag(tagRepository, "lecture");
//         tags.put("lecture", lectureTag);
        
//         Tag videoTag = createTag(tagRepository, "video");
//         tags.put("video", videoTag);
        
//         Tag documentTag = createTag(tagRepository, "document");
//         tags.put("document", documentTag);
        
//         Tag presentationTag = createTag(tagRepository, "presentation");
//         tags.put("presentation", presentationTag);
        
//         log.info("Successfully created {} tags", tags.size());
//     } catch (Exception e) {
//         log.error("Error creating tags: " + e.getMessage(), e);
//     }
    
//     return tags;
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private Map<String, Content> initContent(ContentRepository contentRepository, Map<String, Course> courses, Map<String, Module> modules, Map<String, Tag> tags) {
//     log.info("Creating course content...");
//     Map<String, Content> contents = new HashMap<>();
    
//     try {
//         // Create content for Introduction to CS course
//         if (courses.get("introCS") != null && modules.get("introModule1") != null) {
//             // Intro to Computing Lesson
//             Content introLesson = createContent(
//                 contentRepository,
//                 "Introduction to Web Development", // Using title from mock data
//                 "Fundamental computing concepts and history of computer science",
//                 courses.get("introCS"),
//                 ContentType.LESSON,
//                 "/content/intro-to-computing.html",
//                 "text/html",
//                 150000L,
//                 modules.get("introModule1"),
//                 1
//             );
            
//             if (tags.get("beginner") != null && tags.get("lecture") != null) {
//                 Set<Tag> introTags = new HashSet<>();
//                 introTags.add(tags.get("beginner"));
//                 introTags.add(tags.get("lecture"));
//                 introLesson.setTags(introTags);
//             }
            
//             contentRepository.save(introLesson);
//             contents.put("introLesson", introLesson);
            
//             // HTML Fundamentals Video
//             Content htmlVideo = createContent(
//                 contentRepository,
//                 "HTML5 Fundamentals", // Using title from mock data
//                 "Introduction to HTML markup language",
//                 courses.get("introCS"),
//                 ContentType.VIDEO,
//                 "/content/html-fundamentals.mp4",
//                 "video/mp4",
//                 250000000L,
//                 modules.get("introModule1"),
//                 2
//             );
            
//             if (tags.get("beginner") != null && tags.get("video") != null && tags.get("html-css") != null) {
//                 Set<Tag> htmlTags = new HashSet<>();
//                 htmlTags.add(tags.get("beginner"));
//                 htmlTags.add(tags.get("video"));
//                 htmlTags.add(tags.get("html-css"));
//                 htmlVideo.setTags(htmlTags);
//             }
            
//             contentRepository.save(htmlVideo);
//             contents.put("htmlVideo", htmlVideo);
//         }
        
//         // Create content for Web Development course
//         if (courses.get("webDev") != null && modules.get("webModule2") != null) {
//             // JavaScript Document
//             Content jsDoc = createContent(
//                 contentRepository,
//                 "JavaScript Basics", // Using title from mock data
//                 "Core JavaScript concepts and syntax",
//                 courses.get("webDev"),
//                 ContentType.DOCUMENT,
//                 "/content/javascript-basics.pdf",
//                 "application/pdf",
//                 3500000L,
//                 modules.get("webModule2"),
//                 1
//             );
            
//             if (tags.get("intermediate") != null && tags.get("document") != null && tags.get("javascript") != null) {
//                 Set<Tag> jsTags = new HashSet<>();
//                 jsTags.add(tags.get("intermediate"));
//                 jsTags.add(tags.get("document"));
//                 jsTags.add(tags.get("javascript"));
//                 jsDoc.setTags(jsTags);
//             }
            
//             contentRepository.save(jsDoc);
//             contents.put("jsDoc", jsDoc);
//         }
        
//         // Create content for Data Structures course
//         if (courses.get("dataStructures") != null && modules.get("dsModule1") != null) {
//             // Arrays and Linked Lists presentation
//             Content arraysPresentation = createContent(
//                 contentRepository,
//                 "Arrays vs. Linked Lists",
//                 "Comparison of static and dynamic linear data structures",
//                 courses.get("dataStructures"),
//                 ContentType.PRESENTATION,
//                 "/content/arrays-vs-linked-lists.pptx",
//                 "application/vnd.openxmlformats-officedocument.presentationml.presentation",
//                 4200000L,
//                 modules.get("dsModule1"),
//                 1
//             );
            
//             if (tags.get("intermediate") != null && tags.get("presentation") != null && tags.get("datastructures") != null) {
//                 Set<Tag> arrTags = new HashSet<>();
//                 arrTags.add(tags.get("intermediate"));
//                 arrTags.add(tags.get("presentation"));
//                 arrTags.add(tags.get("datastructures"));
//                 arraysPresentation.setTags(arrTags);
//             }
            
//             contentRepository.save(arraysPresentation);
//             contents.put("arraysPresentation", arraysPresentation);
//         }
        
//         log.info("Successfully created {} content items", contents.size());
//     } catch (Exception e) {
//         log.error("Error creating content: " + e.getMessage(), e);
//     }
    
//     return contents;
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private Map<String, Enrollment> initEnrollments(EnrollmentRepository enrollmentRepository, Map<String, User> users, Map<String, Course> courses) {
//     log.info("Creating enrollments aligned with frontend...");
//     Map<String, Enrollment> enrollments = new HashMap<>();
    
//     try {
//         // Enroll Alex/John in Intro to CS
//         if (users.get("studentJohn") != null && courses.get("introCS") != null) {
//             Enrollment johnIntro = Enrollment.builder()
//                 .student(users.get("studentJohn"))
//                 .course(courses.get("introCS"))
//                 .status(EnrollmentStatus.APPROVED) // Maps to "active" in frontend
//                 .enrollmentDate(LocalDateTime.now().minusDays(30))
//                 .lastAccessedDate(LocalDateTime.now().minusDays(2))
//                 .progress(65.0)
//                 .build();
            
//             johnIntro = enrollmentRepository.save(johnIntro);
//             enrollments.put("johnIntro", johnIntro);
//             enrollments.put("johnAnatomy", johnIntro); // Alias for compatibility
//         }
        
//         // Enroll Alex/John in Data Structures 
//         if (users.get("studentJohn") != null && courses.get("dataStructures") != null) {
//             Enrollment johnData = Enrollment.builder()
//                 .student(users.get("studentJohn"))
//                 .course(courses.get("dataStructures"))
//                 .status(EnrollmentStatus.APPROVED)
//                 .enrollmentDate(LocalDateTime.now().minusDays(28))
//                 .lastAccessedDate(LocalDateTime.now().minusDays(1))
//                 .progress(80.0)
//                 .build();
            
//             johnData = enrollmentRepository.save(johnData);
//             enrollments.put("johnData", johnData);
//             enrollments.put("johnBiochem", johnData); // Alias for compatibility
//         }
        
//         // Enroll Sarah/Jane in Intro to CS with CANCELLED status
//         if (users.get("studentJane") != null && courses.get("introCS") != null) {
//             Enrollment janeIntro = Enrollment.builder()
//                 .student(users.get("studentJane"))
//                 .course(courses.get("introCS"))
//                 .status(EnrollmentStatus.CANCELLED) // Maps to "inactive" in frontend
//                 .enrollmentDate(LocalDateTime.now().minusDays(40))
//                 .lastAccessedDate(LocalDateTime.now().minusDays(35))
//                 .progress(15.0)
//                 .build();
            
//             janeIntro = enrollmentRepository.save(janeIntro);
//             enrollments.put("janeIntro", janeIntro);
//             enrollments.put("janeAnatomy", janeIntro); // Alias for compatibility
//         }
        
//         log.info("Successfully created {} enrollments", enrollments.size());
//     } catch (Exception e) {
//         log.error("Error creating enrollments: " + e.getMessage(), e);
//     }
    
//     return enrollments;
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private void initProgress(ProgressRepository progressRepository, Map<String, User> users, Map<String, Course> courses) {
//     log.info("Creating course progress records...");
    
//     try {
//         // Add progress for Alex/John in Intro to CS - matching enrollment progress
//         if (users.get("studentJohn") != null && courses.get("introCS") != null) {
//             createProgress(
//                 progressRepository,
//                 users.get("studentJohn"),
//                 courses.get("introCS"),
//                 65.0, // Same as enrollment progress
//                 LocalDateTime.now().minusDays(2)
//             );
//         }
        
//         // Add progress for Alex/John in Data Structures
//         if (users.get("studentJohn") != null && courses.get("dataStructures") != null) {
//             createProgress(
//                 progressRepository,
//                 users.get("studentJohn"),
//                 courses.get("dataStructures"),
//                 80.0, // Same as enrollment progress
//                 LocalDateTime.now().minusDays(1)
//             );
//         }
        
//         // Add progress for Sarah/Jane in Intro to CS
//         if (users.get("studentJane") != null && courses.get("introCS") != null) {
//             createProgress(
//                 progressRepository,
//                 users.get("studentJane"),
//                 courses.get("introCS"),
//                 15.0, // Same as enrollment progress
//                 LocalDateTime.now().minusDays(35)
//             );
//         }
        
//         log.info("Successfully created course progress records");
//     } catch (Exception e) {
//         log.error("Error creating progress: " + e.getMessage(), e);
//     }
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private void initContentProgress(ContentProgressRepository contentProgressRepository, Map<String, Enrollment> enrollments, Map<String, Content> contents) {
//     log.info("Creating content progress records...");
    
//     try {
//         // Alex/John's progress in Intro to CS content
//         if (enrollments.get("johnIntro") != null && contents.get("introLesson") != null) {
//             createContentProgress(
//                 contentProgressRepository,
//                 enrollments.get("johnIntro"),
//                 contents.get("introLesson"),
//                 100.0, // Completed this content
//                 LocalDateTime.now().minusDays(25)
//             );
//         }
        
//         if (enrollments.get("johnIntro") != null && contents.get("htmlVideo") != null) {
//             createContentProgress(
//                 contentProgressRepository,
//                 enrollments.get("johnIntro"),
//                 contents.get("htmlVideo"),
//                 75.0, // Partially complete
//                 LocalDateTime.now().minusDays(20)
//             );
//         }
        
//         // Alex/John's progress in Data Structures content
//         if (enrollments.get("johnData") != null && contents.get("arraysPresentation") != null) {
//             createContentProgress(
//                 contentProgressRepository,
//                 enrollments.get("johnData"),
//                 contents.get("arraysPresentation"),
//                 80.0,
//                 LocalDateTime.now().minusDays(15)
//             );
//         }
        
//         // Sarah/Jane's minimal progress in Intro to CS
//         if (enrollments.get("janeIntro") != null && contents.get("introLesson") != null) {
//             createContentProgress(
//                 contentProgressRepository,
//                 enrollments.get("janeIntro"),
//                 contents.get("introLesson"),
//                 15.0,
//                 LocalDateTime.now().minusDays(35)
//             );
//         }
        
//         log.info("Successfully created content progress records");
//     } catch (Exception e) {
//         log.error("Error creating content progress: " + e.getMessage(), e);
//     }
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private Map<String, Quiz> initQuizzes(QuizRepository quizRepository, Map<String, Course> courses) {
//     log.info("Creating quizzes...");
//     Map<String, Quiz> quizzes = new HashMap<>();
    
//     try {
//         // Create quiz for Intro CS course (first course)
//         if (courses.get("introCS") != null) {
//             Quiz midtermQuiz = Quiz.builder()
//                 .title("Midterm Exam")
//                 .description("Comprehensive assessment of first half material")
//                 .timeLimit(120) // 2 hours
//                 .startDate(LocalDateTime.now().plusDays(10))
//                 .endDate(LocalDateTime.now().plusDays(10).plusHours(3))
//                 .passingScore(70.0)
//                 .randomizeQuestions(true)
//                 .published(true)
//                 .course(courses.get("introCS"))
//                 .createdAt(LocalDateTime.now().minusDays(5))
//                 .updatedAt(LocalDateTime.now().minusDays(2))
//                 .build();
                
//             midtermQuiz = quizRepository.save(midtermQuiz);
//             quizzes.put("introCSMidterm", midtermQuiz);
            
//             // Weekly quiz
//             Quiz weeklyQuiz = Quiz.builder()
//                 .title("Weekly Quiz 3")
//                 .description("Short quiz covering material from week 3 lectures")
//                 .timeLimit(30) // 30 minutes
//                 .startDate(LocalDateTime.now().minusDays(2))
//                 .endDate(LocalDateTime.now().plusDays(5))
//                 .passingScore(65.0)
//                 .randomizeQuestions(false)
//                 .published(true)
//                 .course(courses.get("introCS"))
//                 .createdAt(LocalDateTime.now().minusDays(10))
//                 .updatedAt(LocalDateTime.now().minusDays(10))
//                 .build();
                
//             weeklyQuiz = quizRepository.save(weeklyQuiz);
//             quizzes.put("introCSWeekly", weeklyQuiz);
//         }
        
//         // Create quiz for Data Structures course
//         if (courses.get("dataStructures") != null) {
//             Quiz dsQuiz = Quiz.builder()
//                 .title("Data Structures Final Exam")
//                 .description("Comprehensive final exam covering all course material")
//                 .timeLimit(180) // 3 hours
//                 .startDate(LocalDateTime.now().plusMonths(3))
//                 .endDate(LocalDateTime.now().plusMonths(3).plusHours(4))
//                 .passingScore(60.0)
//                 .randomizeQuestions(true)
//                 .published(false) // Not published yet
//                 .course(courses.get("dataStructures"))
//                 .createdAt(LocalDateTime.now().minusDays(1))
//                 .updatedAt(LocalDateTime.now().minusDays(1))
//                 .build();
                
//             dsQuiz = quizRepository.save(dsQuiz);
//             quizzes.put("dsFinal", dsQuiz);
//         }
        
//         log.info("Successfully created {} quizzes", quizzes.size());
//     } catch (Exception e) {
//         log.error("Error creating quizzes: " + e.getMessage(), e);
//     }
    
//     return quizzes;
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private Map<String, Question> initQuestions(QuestionRepository questionRepository, Map<String, Quiz> quizzes) {
//     log.info("Creating quiz questions...");
//     Map<String, Question> questions = new HashMap<>();
    
//     try {
//         // Create questions for intro CS midterm
//         if (quizzes.get("introCSMidterm") != null) {
//             Question q1 = Question.builder()
//                 .text("What does CPU stand for?")
//                 .type(QuestionType.MULTIPLE_CHOICE)
//                 .points(5)
//                 .orderIndex(1)
//                 .feedback("The CPU (Central Processing Unit) is the primary component of a computer that processes instructions.")
//                 .quiz(quizzes.get("introCSMidterm"))
//                 .build();
                
//             q1 = questionRepository.save(q1);
//             questions.put("q1IntroCS", q1);
            
//             Question q2 = Question.builder()
//                 .text("Explain the concept of variables in programming.")
//                 .type(QuestionType.ESSAY)
//                 .points(10)
//                 .orderIndex(2)
//                 .feedback("Variables are named storage locations that hold data that can be modified during program execution.")
//                 .quiz(quizzes.get("introCSMidterm"))
//                 .build();
                
//             q2 = questionRepository.save(q2);
//             questions.put("q2IntroCS", q2);
            
//             Question q3 = Question.builder()
//                 .text("Which of the following is NOT a primitive data type in Java?")
//                 .type(QuestionType.MULTIPLE_CHOICE)
//                 .points(5)
//                 .orderIndex(3)
//                 .feedback("String is a class in Java, not a primitive data type.")
//                 .quiz(quizzes.get("introCSMidterm"))
//                 .build();
                
//             q3 = questionRepository.save(q3);
//             questions.put("q3IntroCS", q3);
//         }
        
//         // Create questions for weekly quiz
//         if (quizzes.get("introCSWeekly") != null) {
//             Question wq1 = Question.builder()
//                 .text("What is the output of the following code snippet?\nint x = 5;\nSystem.out.println(x++);")
//                 .type(QuestionType.MULTIPLE_CHOICE)
//                 .points(2)
//                 .orderIndex(1)
//                 .feedback("The post-increment operator (x++) returns the value before incrementing.")
//                 .quiz(quizzes.get("introCSWeekly"))
//                 .build();
                
//             wq1 = questionRepository.save(wq1);
//             questions.put("wq1Weekly", wq1);
            
//             Question wq2 = Question.builder()
//                 .text("Describe the difference between a while loop and a do-while loop.")
//                 .type(QuestionType.SHORT_ANSWER)
//                 .points(3)
//                 .orderIndex(2)
//                 .feedback("A while loop checks the condition first, while a do-while loop executes at least once before checking.")
//                 .quiz(quizzes.get("introCSWeekly"))
//                 .build();
                
//             wq2 = questionRepository.save(wq2);
//             questions.put("wq2Weekly", wq2);
//         }
        
//         log.info("Successfully created {} questions", questions.size());
//     } catch (Exception e) {
//         log.error("Error creating questions: " + e.getMessage(), e);
//     }
    
//     return questions;
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private void initAnswerOptions(AnswerOptionRepository answerOptionRepository, Map<String, Question> questions) {
//     log.info("Creating answer options...");
    
//     try {
//         // Answer options for CPU question
//         if (questions.get("q1IntroCS") != null) {
//             AnswerOption ao1 = AnswerOption.builder()
//                 .text("Central Processing Unit")
//                 .correct(true)
//                 .feedback("Correct!")
//                 .orderIndex(1)
//                 .question(questions.get("q1IntroCS"))
//                 .build();
                
//             answerOptionRepository.save(ao1);
            
//             AnswerOption ao2 = AnswerOption.builder()
//                 .text("Computer Processing Unit")
//                 .correct(false)
//                 .feedback("Not quite. CPU stands for Central Processing Unit.")
//                 .orderIndex(2)
//                 .question(questions.get("q1IntroCS"))
//                 .build();
                
//             answerOptionRepository.save(ao2);
            
//             AnswerOption ao3 = AnswerOption.builder()
//                 .text("Central Program Unit")
//                 .correct(false)
//                 .feedback("Incorrect. CPU stands for Central Processing Unit.")
//                 .orderIndex(3)
//                 .question(questions.get("q1IntroCS"))
//                 .build();
                
//             answerOptionRepository.save(ao3);
//         }
        
//         // Answer options for data types question
//         if (questions.get("q3IntroCS") != null) {
//             AnswerOption ao1 = AnswerOption.builder()
//                 .text("int")
//                 .correct(false)
//                 .feedback("int is a primitive data type in Java.")
//                 .orderIndex(1)
//                 .question(questions.get("q3IntroCS"))
//                 .build();
                
//             answerOptionRepository.save(ao1);
            
//             AnswerOption ao2 = AnswerOption.builder()
//                 .text("boolean")
//                 .correct(false)
//                 .feedback("boolean is a primitive data type in Java.")
//                 .orderIndex(2)
//                 .question(questions.get("q3IntroCS"))
//                 .build();
                
//             answerOptionRepository.save(ao2);
            
//             AnswerOption ao3 = AnswerOption.builder()
//                 .text("String")
//                 .correct(true)
//                 .feedback("Correct! String is a class, not a primitive data type.")
//                 .orderIndex(3)
//                 .question(questions.get("q3IntroCS"))
//                 .build();
                
//             answerOptionRepository.save(ao3);
            
//             AnswerOption ao4 = AnswerOption.builder()
//                 .text("char")
//                 .correct(false)
//                 .feedback("char is a primitive data type in Java.")
//                 .orderIndex(4)
//                 .question(questions.get("q3IntroCS"))
//                 .build();
                
//             answerOptionRepository.save(ao4);
//         }
        
//         // Weekly quiz answer options
//         if (questions.get("wq1Weekly") != null) {
//             AnswerOption ao1 = AnswerOption.builder()
//                 .text("5")
//                 .correct(true)
//                 .feedback("Correct! Post-increment returns the original value.")
//                 .orderIndex(1)
//                 .question(questions.get("wq1Weekly"))
//                 .build();
                
//             answerOptionRepository.save(ao1);
            
//             AnswerOption ao2 = AnswerOption.builder()
//                 .text("6")
//                 .correct(false)
//                 .feedback("Incorrect. Post-increment (x++) returns the original value, then increments.")
//                 .orderIndex(2)
//                 .question(questions.get("wq1Weekly"))
//                 .build();
                
//             answerOptionRepository.save(ao2);
//         }
        
//         log.info("Successfully created answer options");
//     } catch (Exception e) {
//         log.error("Error creating answer options: " + e.getMessage(), e);
//     }
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private Map<String, QuizAttempt> initQuizAttempts(QuizAttemptRepository quizAttemptRepository, Map<String, Quiz> quizzes, Map<String, User> users) {
//     log.info("Creating quiz attempts...");
//     Map<String, QuizAttempt> attempts = new HashMap<>();
    
//     try {
//         // Create a completed weekly quiz attempt for the student
//         if (quizzes.get("introCSWeekly") != null && users.get("studentJohn") != null) {
//             QuizAttempt attempt = QuizAttempt.builder()
//                 .quiz(quizzes.get("introCSWeekly"))
//                 .student(users.get("studentJohn"))
//                 .startTime(LocalDateTime.now().minusDays(1))
//                 .endTime(LocalDateTime.now().minusDays(1).plusMinutes(20))
//                 .rawScore(4.0) // 4 out of 5 points
//                 .percentageScore(80.0)
//                 .passed(true)
//                 .status(AttemptStatus.COMPLETED)
//                 .build();
                
//             attempt = quizAttemptRepository.save(attempt);
//             attempts.put("johnWeeklyAttempt", attempt);
//         }
        
//         // Create an in-progress attempt for the midterm
//         if (quizzes.get("introCSMidterm") != null && users.get("studentJohn") != null) {
//             QuizAttempt attempt = QuizAttempt.builder()
//                 .quiz(quizzes.get("introCSMidterm"))
//                 .student(users.get("studentJohn"))
//                 .startTime(LocalDateTime.now().minusMinutes(30))
//                 .status(AttemptStatus.IN_PROGRESS)
//                 .build();
                
//             attempt = quizAttemptRepository.save(attempt);
//             attempts.put("johnMidtermAttempt", attempt);
//         }
        
//         log.info("Successfully created {} quiz attempts", attempts.size());
//     } catch (Exception e) {
//         log.error("Error creating quiz attempts: " + e.getMessage(), e);
//     }
    
//     return attempts;
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private void initStudentAnswers(StudentAnswerRepository studentAnswerRepository, Map<String, QuizAttempt> attempts, Map<String, Question> questions, AnswerOptionRepository answerOptionRepository) {
//     log.info("Creating student answers...");
    
//     try {
//         // Create student answers for the completed weekly quiz
//         if (attempts.get("johnWeeklyAttempt") != null && questions.get("wq1Weekly") != null) {
//             // Get the correct answer option for the multiple choice question
//             List<AnswerOption> options = answerOptionRepository.findByQuestionId(questions.get("wq1Weekly").getId());
//             Optional<AnswerOption> correctOption = options.stream().filter(AnswerOption::isCorrect).findFirst();
            
//             if (correctOption.isPresent()) {
//                 StudentAnswer answer = StudentAnswer.builder()
//                     .attempt(attempts.get("johnWeeklyAttempt"))
//                     .question(questions.get("wq1Weekly"))
//                     .selectedOptions(List.of(correctOption.get()))
//                     .score(2.0) // Full points for correct answer
//                     .markedCorrect(true)
//                     .markedManually(false)
//                     .build();
                    
//                 studentAnswerRepository.save(answer);
//             }
//         }
        
//         // For short answer question
//         if (attempts.get("johnWeeklyAttempt") != null && questions.get("wq2Weekly") != null) {
//             StudentAnswer answer = StudentAnswer.builder()
//                 .attempt(attempts.get("johnWeeklyAttempt"))
//                 .question(questions.get("wq2Weekly"))
//                 .selectedOptions(new ArrayList<>()) // No selected options for short answer
//                 .textAnswer("A while loop checks the condition first, but a do-while loop executes the code at least once before checking the condition.")
//                 .score(2.0) // Partial points (out of 3)
//                 .markedCorrect(false)
//                 .markedManually(true)
//                 .instructorFeedback("Good explanation, but could be more detailed about edge cases.")
//                 .build();
                
//             studentAnswerRepository.save(answer);
//         }
        
//         log.info("Successfully created student answers");
//     } catch (Exception e) {
//         log.error("Error creating student answers: " + e.getMessage(), e);
//     }
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private Map<String, Assignment> initAssignments(AssignmentRepository assignmentRepository, Map<String, Course> courses, Map<String, User> users) {
//     log.info("Creating assignments...");
//     Map<String, Assignment> assignments = new HashMap<>();
    
//     try {
//         // Create assignment for Intro CS course
//         if (courses.get("introCS") != null) {
//             Assignment programming = Assignment.builder()
//                 .title("Programming Assignment 1")
//                 .description("Implement a simple calculator program using Java")
//                 .courseId(courses.get("introCS").getId())
//                 .dueDate(LocalDateTime.now().plusDays(7))
//                 .maxScore(100)
//                 .published(true)
//                 .createdBy(users.get("janeSmith").getFullName())
//                 .createdAt(LocalDateTime.now().minusDays(10))
//                 .updatedAt(LocalDateTime.now().minusDays(10))
//                 .build();
                
//             programming = assignmentRepository.save(programming);
//             assignments.put("introCSProgramming", programming);
            
//             Assignment research = Assignment.builder()
//                 .title("Research Paper")
//                 .description("Write a 5-page research paper on the history of computing")
//                 .courseId(courses.get("introCS").getId())
//                 .dueDate(LocalDateTime.now().plusDays(14))
//                 .maxScore(50)
//                 .published(true)
//                 .createdBy(users.get("janeSmith").getFullName())
//                 .createdAt(LocalDateTime.now().minusDays(5))
//                 .updatedAt(LocalDateTime.now().minusDays(3))
//                 .build();
                
//             research = assignmentRepository.save(research);
//             assignments.put("introCSResearch", research);
//         }
        
//         // Create assignment for Data Structures course
//         if (courses.get("dataStructures") != null) {
//             Assignment project = Assignment.builder()
//                 .title("Database Design Project")
//                 .description("Design and implement a database schema for a real-world scenario")
//                 .courseId(courses.get("dataStructures").getId())
//                 .dueDate(LocalDateTime.now().plusDays(21))
//                 .maxScore(100)
//                 .published(true)
//                 .createdBy(users.get("michaelChen").getFullName())
//                 .createdAt(LocalDateTime.now().minusDays(7))
//                 .updatedAt(LocalDateTime.now().minusDays(7))
//                 .build();
                
//             project = assignmentRepository.save(project);
//             assignments.put("dsProject", project);
            
//             // Draft (unpublished) assignment
//             Assignment draft = Assignment.builder()
//                 .title("Algorithm Analysis Exercise")
//                 .description("Draft assignment - not yet ready for students")
//                 .courseId(courses.get("dataStructures").getId())
//                 .dueDate(LocalDateTime.now().plusDays(30))
//                 .maxScore(75)
//                 .published(false)
//                 .createdBy(users.get("michaelChen").getFullName())
//                 .createdAt(LocalDateTime.now().minusDays(2))
//                 .updatedAt(LocalDateTime.now().minusHours(5))
//                 .build();
                
//             draft = assignmentRepository.save(draft);
//             assignments.put("dsDraft", draft);
//         }
        
//         log.info("Successfully created {} assignments", assignments.size());
//     } catch (Exception e) {
//         log.error("Error creating assignments: " + e.getMessage(), e);
//     }
    
//     return assignments;
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private void initScores(ScoreRepository scoreRepository, Map<String, User> users, Map<String, Assignment> assignments) {
//     log.info("Creating assignment scores...");
    
//     try {
//         // Create score for the programming assignment
//         if (users.get("studentJohn") != null && assignments.get("introCSProgramming") != null) {
//             Score score = Score.builder()
//                 .assignmentId(assignments.get("introCSProgramming").getId())
//                 .studentId(users.get("studentJohn").getId())
//                 .score(85.0)
//                 .feedback("Good implementation, but could use better exception handling.")
//                 .gradedBy(users.get("janeSmith").getFullName())
//                 .gradedAt(LocalDateTime.now().minusDays(1))
//                 .build();
                
//             scoreRepository.save(score);
//         }
        
//         log.info("Successfully created assignment scores");
//     } catch (Exception e) {
//         log.error("Error creating scores: " + e.getMessage(), e);
//     }
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private void initSubmissions(SubmissionRepository submissionRepository, Map<String, Assignment> assignments, Map<String, User> users) {
//     log.info("Creating assignment submissions...");
    
//     try {
//         // Create submission for programming assignment
//         if (assignments.get("introCSProgramming") != null && users.get("studentJohn") != null) {
//             Submission submission = Submission.builder()
//                 .assignmentId(assignments.get("introCSProgramming").getId())
//                 .studentId(users.get("studentJohn").getId())
//                 .filePath("/submissions/john_calculator.zip")
//                 .submittedAt(LocalDateTime.now().minusDays(2))
//                 .build();
                
//             submissionRepository.save(submission);
//         }
        
//         // Create submission for research paper
//         if (assignments.get("introCSResearch") != null && users.get("studentJohn") != null) {
//             Submission submission = Submission.builder()
//                 .assignmentId(assignments.get("introCSResearch").getId())
//                 .studentId(users.get("studentJohn").getId())
//                 .filePath("/submissions/john_research_paper.pdf")
//                 .submittedAt(LocalDateTime.now().minusDays(1))
//                 .build();
                
//             submissionRepository.save(submission);
//         }
        
//         log.info("Successfully created assignment submissions");
//     } catch (Exception e) {
//         log.error("Error creating submissions: " + e.getMessage(), e);
//     }
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private Map<String, ForumThread> initForumThreads(ForumThreadRepository forumThreadRepository, Map<String, Course> courses, Map<String, User> users) {
//     log.info("Creating forum threads...");
//     Map<String, ForumThread> threads = new HashMap<>();
    
//     try {
//         // Create thread for Intro CS course
//         if (courses.get("introCS") != null && users.get("studentJohn") != null) {
//             ForumThread helpThread = ForumThread.builder()
//                 .title("Help with Assignment 1")
//                 .content("I'm having trouble with the calculator program. Can someone explain how to handle division by zero?")
//                 .courseId(courses.get("introCS").getId())
//                 .authorId(users.get("studentJohn").getId())
//                 .authorName(users.get("studentJohn").getFullName())
//                 .createdAt(LocalDateTime.now().minusDays(3))
//                 .updatedAt(LocalDateTime.now().minusDays(3))
//                 .pinned(false)
//                 .build();
                
//             helpThread = forumThreadRepository.save(helpThread);
//             threads.put("helpThread", helpThread);
//         }
        
//         // Create announcement thread by instructor
//         if (courses.get("introCS") != null && users.get("janeSmith") != null) {
//             ForumThread announcement = ForumThread.builder()
//                 .title("Important: Midterm Exam Details")
//                 .content("The midterm exam will be held next week. Please review chapters 1-5 thoroughly. The format will include multiple choice and essay questions.")
//                 .courseId(courses.get("introCS").getId())
//                 .authorId(users.get("janeSmith").getId())
//                 .authorName(users.get("janeSmith").getFullName())
//                 .createdAt(LocalDateTime.now().minusDays(5))
//                 .updatedAt(LocalDateTime.now().minusDays(5))
//                 .pinned(true)
//                 .build();
                
//             announcement = forumThreadRepository.save(announcement);
//             threads.put("announcement", announcement);
//         }
        
//         // Create discussion thread for Data Structures course
//         if (courses.get("dataStructures") != null && users.get("studentJohn") != null) {
//             ForumThread discussionThread = ForumThread.builder()
//                 .title("Linked Lists vs Arrays - Performance Comparison")
//                 .content("I've been comparing the performance of linked lists and arrays for different operations. Has anyone else done this comparison?")
//                 .courseId(courses.get("dataStructures").getId())
//                 .authorId(users.get("studentJohn").getId())
//                 .authorName(users.get("studentJohn").getFullName())
//                 .createdAt(LocalDateTime.now().minusDays(7))
//                 .updatedAt(LocalDateTime.now().minusDays(5))
//                 .pinned(false)
//                 .build();
                
//             discussionThread = forumThreadRepository.save(discussionThread);
//             threads.put("discussionThread", discussionThread);
//         }
        
//         log.info("Successfully created {} forum threads", threads.size());
//     } catch (Exception e) {
//         log.error("Error creating forum threads: " + e.getMessage(), e);
//     }
    
//     return threads;
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private void initForumPosts(ForumPostRepository forumPostRepository, Map<String, ForumThread> threads, Map<String, User> users) {
//     log.info("Creating forum posts...");
    
//     try {
//         // Create response to help thread from instructor
//         if (threads.get("helpThread") != null && users.get("janeSmith") != null) {
//             ForumPost instructorResponse = ForumPost.builder()
//                 .content("You should use a try-catch block to handle the division by zero exception. Check the lecture notes from Week 2 for examples.")
//                 .threadId(threads.get("helpThread").getId())
//                 .authorId(users.get("janeSmith").getId())
//                 .authorName(users.get("janeSmith").getFullName())
//                 .createdAt(LocalDateTime.now().minusDays(3).plusHours(2))
//                 .updatedAt(LocalDateTime.now().minusDays(3).plusHours(2))
//                 .build();
                
//             forumPostRepository.save(instructorResponse);
//         }
        
//         // Create student response to announcement
//         if (threads.get("announcement") != null && users.get("studentJohn") != null) {
//             ForumPost studentQuestion = ForumPost.builder()
//                 .content("Will the exam be open book or closed book?")
//                 .threadId(threads.get("announcement").getId())
//                 .authorId(users.get("studentJohn").getId())
//                 .authorName(users.get("studentJohn").getFullName())
//                 .createdAt(LocalDateTime.now().minusDays(4))
//                 .updatedAt(LocalDateTime.now().minusDays(4))
//                 .build();
                
//             forumPostRepository.save(studentQuestion);
            
//             // Instructor reply to student question
//             if (users.get("janeSmith") != null) {
//                 ForumPost instructorReply = ForumPost.builder()
//                     .content("The exam will be closed book. Only a basic calculator will be permitted.")
//                     .threadId(threads.get("announcement").getId())
//                     .authorId(users.get("janeSmith").getId())
//                     .authorName(users.get("janeSmith").getFullName())
//                     .createdAt(LocalDateTime.now().minusDays(4).plusHours(1))
//                     .updatedAt(LocalDateTime.now().minusDays(4).plusHours(1))
//                     .build();
                    
//                 forumPostRepository.save(instructorReply);
//             }
//         }
        
//         // Create posts on discussion thread
//         if (threads.get("discussionThread") != null && users.get("michaelChen") != null) {
//             ForumPost instructorComment = ForumPost.builder()
//                 .content("Great topic! Arrays have O(1) access time but linked lists have O(1) insertion at the beginning. You might want to benchmark these operations to see the actual performance differences.")
//                 .threadId(threads.get("discussionThread").getId())
//                 .authorId(users.get("michaelChen").getId())
//                 .authorName(users.get("michaelChen").getFullName())
//                 .createdAt(LocalDateTime.now().minusDays(6))
//                 .updatedAt(LocalDateTime.now().minusDays(6))
//                 .build();
                
//             forumPostRepository.save(instructorComment);
//         }
        
//         log.info("Successfully created forum posts");
//     } catch (Exception e) {
//         log.error("Error creating forum posts: " + e.getMessage(), e);
//     }
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private void initNotifications(NotificationRepository notificationRepository, 
//                              Map<String, User> users, 
//                              Map<String, Course> courses,
//                              Map<String, Quiz> quizzes,
//                              Map<String, Assignment> assignments,
//                              Map<String, ForumThread> threads) {
//     log.info("Creating notifications...");
    
//     try {
//         // Assignment due notification
//         if (users.get("studentJohn") != null && assignments.get("introCSProgramming") != null) {
//             Notification assignmentNotification = Notification.builder()
//                 .userId(users.get("studentJohn").getId())
//                 .title("Assignment Due Soon")
//                 .message("Programming Assignment 1 is due in 7 days.")
//                 .type(NotificationType.ASSIGNMENT_DUE)
//                 .read(false)
//                 .createdAt(LocalDateTime.now().minusDays(1))
//                 .relatedId(assignments.get("introCSProgramming").getId())
//                 .build();
                
//             notificationRepository.save(assignmentNotification);
//         }
        
//         // Quiz availability notification
//         if (users.get("studentJohn") != null && quizzes.get("introCSWeekly") != null) {
//             Notification quizNotification = Notification.builder()
//                 .userId(users.get("studentJohn").getId())
//                 .title("New Quiz Available")
//                 .message("Weekly Quiz 3 is now available. You have until " + quizzes.get("introCSWeekly").getEndDate().toLocalDate() + " to complete it.")
//                 .type(NotificationType.QUIZ_AVAILABLE)
//                 .read(true)
//                 .createdAt(LocalDateTime.now().minusDays(2))
//                 .relatedId(quizzes.get("introCSWeekly").getId())
//                 .build();
                
//             notificationRepository.save(quizNotification);
//         }
        
//         // Forum reply notification
//         if (users.get("studentJohn") != null && threads.get("helpThread") != null) {
//             Notification forumNotification = Notification.builder()
//                 .userId(users.get("studentJohn").getId())
//                 .title("New Reply to Your Thread")
//                 .message("Dr. Jane Smith replied to your thread 'Help with Assignment 1'")
//                 .type(NotificationType.FORUM_REPLY)
//                 .read(false)
//                 .createdAt(LocalDateTime.now().minusDays(3).plusHours(2))
//                 .relatedId(threads.get("helpThread").getId())
//                 .build();
                
//             notificationRepository.save(forumNotification);
//         }
        
//         // Quiz score notification
//         if (users.get("studentJohn") != null && quizzes.get("introCSWeekly") != null) {
//             Notification scoreNotification = Notification.builder()
//                 .userId(users.get("studentJohn").getId())
//                 .title("Quiz Score Available")
//                 .message("Your score for Weekly Quiz 3 is now available.")
//                 .type(NotificationType.QUIZ_GRADED)
//                 .read(false)
//                 .createdAt(LocalDateTime.now().minusHours(12))
//                 .relatedId(quizzes.get("introCSWeekly").getId())
//                 .build();
                
//             notificationRepository.save(scoreNotification);
//         }
        
//         // Announcement notification for instructor
//         if (users.get("janeSmith") != null && courses.get("introCS") != null) {
//             Notification announcementNotification = Notification.builder()
//                 .userId(users.get("janeSmith").getId())
//                 .title("Announcement Posted")
//                 .message("Your announcement 'Important: Midterm Exam Details' has been posted to the course forum.")
//                 .type(NotificationType.ANNOUNCEMENT)
//                 .read(true)
//                 .createdAt(LocalDateTime.now().minusDays(5))
//                 .relatedId(courses.get("introCS").getId())
//                 .build();
                
//             notificationRepository.save(announcementNotification);
//         }
        
//         log.info("Successfully created notifications");
//     } catch (Exception e) {
//         log.error("Error creating notifications: " + e.getMessage(), e);
//     }
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private void initNotificationPreferences(NotificationPreferenceRepository notificationPreferenceRepository, Map<String, User> users) {
//     log.info("Creating notification preferences...");
    
//     try {
//         // Set preferences for student
//         if (users.get("studentJohn") != null) {
//             NotificationPreference assignmentPref = NotificationPreference.builder()
//                 .userId(users.get("studentJohn").getId())
//                 .type(NotificationType.ASSIGNMENT_DUE)
//                 .email(true)
//                 .push(true)
//                 .inApp(true)
//                 .build();
                
//             notificationPreferenceRepository.save(assignmentPref);
            
//             NotificationPreference quizPref = NotificationPreference.builder()
//                 .userId(users.get("studentJohn").getId())
//                 .type(NotificationType.QUIZ_AVAILABLE)
//                 .email(true)
//                 .push(true)
//                 .inApp(true)
//                 .build();
                
//             notificationPreferenceRepository.save(quizPref);
            
//             NotificationPreference forumPref = NotificationPreference.builder()
//                 .userId(users.get("studentJohn").getId())
//                 .type(NotificationType.FORUM_REPLY)
//                 .email(true)
//                 .push(false)
//                 .inApp(true)
//                 .build();
                
//             notificationPreferenceRepository.save(forumPref);
            
//             NotificationPreference gradePref = NotificationPreference.builder()
//                 .userId(users.get("studentJohn").getId())
//                 .type(NotificationType.GRADE_POSTED)
//                 .email(true)
//                 .push(true)
//                 .inApp(true)
//                 .build();
                
//             notificationPreferenceRepository.save(gradePref);
//         }
        
//         // Set preferences for instructor
//         if (users.get("janeSmith") != null) {
//             NotificationPreference submissionPref = NotificationPreference.builder()
//                 .userId(users.get("janeSmith").getId())
//                 .type(NotificationType.SUBMISSION_RECEIVED)
//                 .email(true)
//                 .push(false)
//                 .inApp(true)
//                 .build();
                
//             notificationPreferenceRepository.save(submissionPref);
            
//             NotificationPreference announcementPref = NotificationPreference.builder()
//                 .userId(users.get("janeSmith").getId())
//                 .type(NotificationType.ANNOUNCEMENT)
//                 .email(false)
//                 .push(false)
//                 .inApp(true)
//                 .build();
                
//             notificationPreferenceRepository.save(announcementPref);
            
//             NotificationPreference forumPref = NotificationPreference.builder()
//                 .userId(users.get("janeSmith").getId())
//                 .type(NotificationType.FORUM_REPLY)
//                 .email(true)
//                 .push(true)
//                 .inApp(true)
//                 .build();
                
//             notificationPreferenceRepository.save(forumPref);
//         }
        
//         log.info("Successfully created notification preferences");
//     } catch (Exception e) {
//         log.error("Error creating notification preferences: " + e.getMessage(), e);
//     }
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private Map<String, Role> createRolesAndPermissions(RoleRepository roleRepository, PermissionRepository permissionRepository) {
//     log.info("Creating core roles and permissions...");
//     Map<String, Role> roles = new HashMap<>();
    
//     try {
//         // Create core permissions
//         Permission userManage = createPermission(permissionRepository, "user:manage", "Manage user accounts");
//         Permission courseManage = createPermission(permissionRepository, "course:manage", "Manage courses");
//         Permission contentManage = createPermission(permissionRepository, "content:manage", "Manage course content");
//         Permission enrollmentManage = createPermission(permissionRepository, "enrollment:manage", "Manage course enrollments");
//         Permission gradeManage = createPermission(permissionRepository, "grade:manage", "Manage grades");
//         Permission forumManage = createPermission(permissionRepository, "forum:manage", "Manage forum discussions");
        
//         // Create read permissions
//         Permission userRead = createPermission(permissionRepository, "user:read", "View user information");
//         Permission courseRead = createPermission(permissionRepository, "course:read", "View course information");
//         Permission contentRead = createPermission(permissionRepository, "content:read", "View course content");
//         Permission gradeRead = createPermission(permissionRepository, "grade:read", "View grades");
//         Permission forumRead = createPermission(permissionRepository, "forum:read", "View forum discussions");
        
//         // Create ADMIN role
//         Role adminRole = createRole(roleRepository, "ADMIN", "System Administrator");
//         adminRole.setPermissions(Set.of(
//             userManage, courseManage, contentManage, enrollmentManage, 
//             gradeManage, forumManage, userRead, courseRead, contentRead, gradeRead, forumRead
//         ));
//         roleRepository.save(adminRole);
//         roles.put("ADMIN", adminRole);
        
//         // Create INSTRUCTOR role
//         Role instructorRole = createRole(roleRepository, "INSTRUCTOR", "Course Instructor");
//         instructorRole.setPermissions(Set.of(
//             contentManage, enrollmentManage, gradeManage, forumManage,
//             userRead, courseRead, contentRead, gradeRead, forumRead
//         ));
//         roleRepository.save(instructorRole);
//         roles.put("INSTRUCTOR", instructorRole);
        
//         // Create STUDENT role
//         Role studentRole = createRole(roleRepository, "STUDENT", "Student");
//         studentRole.setPermissions(Set.of(
//             courseRead, contentRead, gradeRead, forumRead
//         ));
//         roleRepository.save(studentRole);
//         roles.put("STUDENT", studentRole);
        
//         log.info("Core roles and permissions created successfully");
//     } catch (Exception e) {
//         log.error("Error creating roles and permissions: " + e.getMessage(), e);
//     }
    
//     return roles;
// }

// private Permission createPermission(PermissionRepository repository, String name, String description) {
//     Permission permission = new Permission();
//     permission.setName(name);
//     permission.setDescription(description);
//     return repository.save(permission);
// }

// private Role createRole(RoleRepository repository, String name, String description) {
//     Role role = new Role();
//     role.setName(name);
//     role.setDescription(description);
//     return repository.save(role);
// }

// private void createUserProfile(UserProfileRepository repository, User user, String firstName, String lastName, 
//                               String phone, String bio) {
//     UserProfile profile = new UserProfile();
//     profile.setUser(user);
//     profile.setFirstName(firstName);
//     profile.setLastName(lastName);
//     profile.setPhone(phone);
//     profile.setBio(bio);
//     repository.save(profile);
// }

// private Course createCourse(CourseRepository repository, String title, String description, User instructor, 
//                            Department department, int capacity) {
//     Course course = new Course();
//     course.setTitle(title);
//     course.setDescription(description);
//     course.setInstructor(instructor);
//     course.setDepartment(department);
//     course.setCapacity(capacity);
//     course.setActive(true); // Using setActive instead
//     course.setCreatedAt(LocalDateTime.now());
//     course.setUpdatedAt(LocalDateTime.now());
//     return repository.save(course);
// }

// private Module createModule(ModuleRepository repository, String title, String description, int orderIndex) {
//     Module module = new Module();
//     module.setTitle(title);
//     module.setDescription(description);
//     module.setOrderIndex(orderIndex);
//     module.setCreatedAt(LocalDateTime.now());
//     module.setUpdatedAt(LocalDateTime.now());
//     return repository.save(module);
// }

// private Tag createTag(TagRepository repository, String name) {
//     Tag tag = new Tag();
//     tag.setName(name);
//     return repository.save(tag);
// }

// private Content createContent(ContentRepository repository, String title, String description, 
//                             Course course, ContentType type, String filePath, String mimeType, 
//                             Long fileSize, Module module, int orderIndex) {
//     Content content = new Content();
//     content.setTitle(title);
//     content.setDescription(description);
//     content.setCourse(course);
//     content.setType(type);
//     content.setFilePath(filePath);
//     content.setMimeType(mimeType);
//     content.setFileSize(fileSize);
//     content.setModule(module);
//     content.setSequence(orderIndex); // Changed from setOrderIndex
//     content.setCreatedAt(LocalDateTime.now());
//     content.setUpdatedAt(LocalDateTime.now());
//     return repository.save(content);
// }

// private void createProgress(ProgressRepository repository, User user, Course course, 
//                           Double percentage, LocalDateTime lastUpdated) {
//     Progress progress = new Progress();
//     progress.setUser(user);
//     progress.setCourse(course);
//     progress.setPercentage(percentage);
//     progress.setLastUpdated(lastUpdated);
//     repository.save(progress);
// }

// private void createContentProgress(ContentProgressRepository repository, Enrollment enrollment,
//                                  Content content, Double percentage, LocalDateTime lastAccessed) {
//     ContentProgress progress = new ContentProgress();
//     progress.setEnrollment(enrollment);
//     progress.setContent(content);
//     progress.setCompletionPercentage(percentage); // Changed from setPercentage
//     progress.setLastAccessedDate(lastAccessed); // Changed from setLastAccessed
//     repository.save(progress);
// }

// @Transactional(propagation = Propagation.REQUIRES_NEW)
// private Map<String, Department> initDepartments(DepartmentRepository departmentRepository) {
//     log.info("Creating departments...");
//     Map<String, Department> departments = new HashMap<>();
    
//     try {
//         // Create academic departments
//         Department computerScience = new Department();
//         computerScience.setName("Computer Science");
//         computerScience.setDescription("Department of Computer Science and Engineering");
//         computerScience.setCode("CS");
//         computerScience = departmentRepository.save(computerScience);
//         departments.put("computerScience", computerScience);
        
//         Department mathematics = new Department();
//         mathematics.setName("Mathematics");
//         mathematics.setDescription("Department of Mathematics");
//         mathematics.setCode("MATH");
//         mathematics = departmentRepository.save(mathematics);
//         departments.put("mathematics", mathematics);
        
//         Department physics = new Department();
//         physics.setName("Physics");
//         physics.setDescription("Department of Physics and Astronomy");
//         physics.setCode("PHYS");
//         physics = departmentRepository.save(physics);
//         departments.put("physics", physics);
        
//         // Create administrative departments
//         Department administration = new Department();
//         administration.setName("Administration");
//         administration.setDescription("Administrative department");
//         administration.setCode("ADMIN");
//         administration = departmentRepository.save(administration);
//         departments.put("administration", administration);
        
//         // For backward compatibility with the older domain-specific names
//         departments.put("anatomy", computerScience);
//         departments.put("biochemistry", physics);
//         departments.put("medFoundation", mathematics);
        
//         log.info("Successfully created {} departments", departments.size());
//     } catch (Exception e) {
//         log.error("Error creating departments: " + e.getMessage(), e);
//     }
    
//     return departments;
// }
