package org.example.elearningbe.course_tracking.course_enrollment;
import org.example.elearningbe.user.entities.User;
import org.example.elearningbe.course.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findByUser(User user);

    Optional<CourseEnrollment> findByUserAndCourse(User user, Course course);
}
