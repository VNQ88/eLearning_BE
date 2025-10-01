package org.example.elearningbe.course_tracking.lesson_progress;
import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.common.BaseEntity;
import org.example.elearningbe.common.enumerate.ProgressStatus;
import org.example.elearningbe.course_tracking.course_enrollment.CourseEnrollment;
import org.example.elearningbe.lesson.entities.Lesson;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "lesson_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_enrollment_lesson",
                columnNames = {"enrollment_id", "lesson_id"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private CourseEnrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status; // NOT_STARTED, IN_PROGRESS, COMPLETED

    @Column(name = "progress_percent", nullable = false)
    private Double progressPercent = 0.0;

    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;
}
