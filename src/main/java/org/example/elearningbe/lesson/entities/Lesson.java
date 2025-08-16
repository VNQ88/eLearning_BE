package org.example.elearningbe.lesson.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.chapter.entities.Chapter;
import org.example.elearningbe.common.BaseEntity;
import org.example.elearningbe.course.entities.Course;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(name = "lesson")
@NoArgsConstructor
@AllArgsConstructor
public class Lesson extends BaseEntity {
    @Column(length = 100, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String image;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String video;

    @ManyToOne
    @JoinColumn(name = "courseId", nullable = false)
    private Course course;

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;
}
