package org.example.elearningbe.course;

import org.example.elearningbe.course.entities.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // Additional query methods can be defined here if needed

    Page<Course> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
