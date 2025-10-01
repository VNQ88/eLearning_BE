package org.example.elearningbe.course_tracking.lesson_progress;
import org.example.elearningbe.course_tracking.course_enrollment.CourseEnrollment;
import org.example.elearningbe.lesson.entities.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByEnrollmentAndLesson(CourseEnrollment enrollment, Lesson lesson);

    List<LessonProgress> findByEnrollment(CourseEnrollment enrollment);
}

