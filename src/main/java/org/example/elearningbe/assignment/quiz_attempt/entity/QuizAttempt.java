package org.example.elearningbe.assignment.quiz_attempt.entity;
import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.assignment.quiz.Quiz;
import org.example.elearningbe.user.entities.User;
import org.example.elearningbe.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer score;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers;
}

