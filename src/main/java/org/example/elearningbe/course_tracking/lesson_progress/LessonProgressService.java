package org.example.elearningbe.course_tracking.lesson_progress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.common.enumerate.EnrollmentStatus;
import org.example.elearningbe.common.enumerate.ProgressStatus;
import org.example.elearningbe.course.CourseRepository;
import org.example.elearningbe.course_tracking.course_enrollment.CourseEnrollment;
import org.example.elearningbe.course_tracking.course_enrollment.CourseEnrollmentRepository;
import org.example.elearningbe.exception.ResourceNotFoundException;
import org.example.elearningbe.lesson.entities.Lesson;
import org.example.elearningbe.lesson.LessonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonProgressService {
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;

    /* ============ API METHODS ============ */

    @Transactional(readOnly = true)
    public LessonProgressResponse getLessonProgress(Long enrollmentId, Long lessonId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        LessonProgress lp = lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson)
                .orElseThrow(() -> new ResourceNotFoundException("LessonProgress not found"));

        return mapToLessonProgressResponse(lp);
    }

    @Transactional
    public LessonProgressResponse updateLessonProgress(Long enrollmentId, Long lessonId, UpdateLessonProgressRequest req) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        LessonProgress lp = lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson)
                .orElseGet(() -> LessonProgress.builder()
                        .enrollment(enrollment)
                        .lesson(lesson)
                        .status(ProgressStatus.NOT_STARTED)
                        .progressPercent(0.0)
                        .build());

        // Update fields
        lp.setProgressPercent(req.getProgressPercent());
        lp.setStatus(ProgressStatus.valueOf(req.getStatus()));
        lp.setLastAccessTime(LocalDateTime.now());

        lessonProgressRepository.save(lp);

        // Recalculate enrollment progress
        recalcEnrollmentProgress(enrollment);

        return mapToLessonProgressResponse(lp);
    }

    @Transactional
    public LessonProgressResponse markLessonCompleted(Long enrollmentId, Long lessonId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        LessonProgress lp = lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson)
                .orElseGet(() -> LessonProgress.builder()
                        .enrollment(enrollment)
                        .lesson(lesson)
                        .status(ProgressStatus.NOT_STARTED)
                        .progressPercent(0.0)
                        .build());

        lp.setStatus(ProgressStatus.COMPLETED);
        lp.setProgressPercent(100.0);
        lp.setLastAccessTime(LocalDateTime.now());

        lessonProgressRepository.save(lp);

        // Recalculate enrollment progress
        recalcEnrollmentProgress(enrollment);

        return mapToLessonProgressResponse(lp);
    }


    /* ============ HELPER METHODS ============ */
    private void recalcEnrollmentProgress(CourseEnrollment enrollment) {
        List<LessonProgress> progresses = lessonProgressRepository.findByEnrollment(enrollment);

        int totalLessons = progresses.size();
        if (totalLessons == 0) {
            enrollment.setProgressPercent(0.0);
            return;
        }

        long completed = progresses.stream()
                .filter(lp -> lp.getStatus() == ProgressStatus.COMPLETED)
                .count();

        double percent = (completed * 100.0) / totalLessons;
        enrollment.setProgressPercent(percent);

        if (percent > 0 && enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
            enrollment.setStatus(EnrollmentStatus.IN_PROGRESS);
            enrollment.setStartedAt(LocalDateTime.now());
        }
        if (percent == 100.0) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
        }

        enrollmentRepository.save(enrollment);
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
}

