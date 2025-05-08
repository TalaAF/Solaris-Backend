@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/available/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<List<UserDTO>> getAvailableStudentsForCourse(@PathVariable Long courseId) {
        // Get all students with STUDENT role
        List<User> allStudents = userRepository.findByRoleName("STUDENT");
        
        // Get IDs of students already enrolled in this course
        List<Long> enrolledStudentIds = enrollmentRepository.findByCourseId(courseId).stream()
            .map(e -> e.getStudent().getId())
            .collect(Collectors.toList());
        
        // Filter out enrolled students
        List<UserDTO> availableStudents = allStudents.stream()
            .filter(student -> !enrolledStudentIds.contains(student.getId()))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(availableStudents);
    }
    
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .roleNames(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()))
            .build();
    }
}