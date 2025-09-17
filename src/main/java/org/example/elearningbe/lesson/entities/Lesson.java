package org.example.elearningbe.lesson.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.chapter.entities.Chapter;
import org.example.elearningbe.common.BaseEntity;
import org.example.elearningbe.common.enumerate.LessonType;
import org.example.elearningbe.course.entities.Course;

@Entity
@Table(name = "lesson")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson extends BaseEntity {
    @Column(length = 100, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // optional thumbnail (ảnh minh họa cho bài học)
    @Column(columnDefinition = "TEXT")
    private String imageObjectKey;

    // objectKey duy nhất cho tài nguyên (video hoặc document)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String resourceObjectKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false, length = 20)
    private LessonType type;   // VIDEO hoặc DOCUMENT

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;
}
