package org.example.elearningbe.course_tracking.course_enrollment;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.course.entities.Course;
import org.example.elearningbe.course.CourseRepository;
import org.example.elearningbe.course_tracking.lesson_progress.LessonProgress;
import org.example.elearningbe.course_tracking.lesson_progress.LessonProgressResponse;
import org.example.elearningbe.exception.ResourceNotFoundException;
import org.example.elearningbe.user.UserRepository;
import org.example.elearningbe.user.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /* ============ API METHODS ============ */

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments() {
        User user = getCurrentUser();
        List<CourseEnrollment> enrollments = enrollmentRepository.findByUser(user);
        return enrollments.stream().map(this::mapToEnrollmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public EnrollmentDetailResponse getEnrollmentDetail(Long courseId) {
        User user = getCurrentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        CourseEnrollment enrollment = enrollmentRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        List<LessonProgressResponse> lessonProgresses = enrollment.getId() != null
                ? enrollment.getLessonProgresses().stream()
                .map(this::mapToLessonProgressResponse)
                .toList()
                : List.of();

        return EnrollmentDetailResponse.builder()
                .enrollment(mapToEnrollmentResponse(enrollment))
                .lessons(lessonProgresses)
                .build();
    }

    /* ============ HELPER MAPPERS ============ */

    private EnrollmentResponse mapToEnrollmentResponse(CourseEnrollment e) {
        return EnrollmentResponse.builder()
                .enrollmentId(e.getId())
                .courseId(e.getCourse().getId())
                .courseTitle(e.getCourse().getTitle())
                .ownerEmail(e.getCourse().getOwner().getEmail())
                .status(e.getStatus().name())
                .progressPercent(e.getProgressPercent())
                .startedAt(e.getStartedAt())
                .completedAt(e.getCompletedAt())
                .build();
    }

    private LessonProgressResponse mapToLessonProgressResponse(LessonProgress lp) {
        return LessonProgressResponse.builder()
                .lessonProgressId(lp.getId())
                .lessonId(lp.getLesson().getId())
                .lessonTitle(lp.getLesson().getTitle())
                .status(lp.getStatus().name())
                .progressPercent(lp.getProgressPercent())
                .lastAccessTime(lp.getLastAccessTime())
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}

