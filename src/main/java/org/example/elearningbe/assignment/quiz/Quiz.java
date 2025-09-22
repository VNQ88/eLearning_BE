package org.example.elearningbe.assignment.quiz;

import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.common.BaseEntity;
import org.example.elearningbe.lesson.entities.Lesson;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quiz extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    @Column(name = "pass_score")
    private Integer passScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
}

