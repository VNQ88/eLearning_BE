package org.example.elearningbe.assignment.quiz_attempt.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.assignment.question.Choice;
import org.example.elearningbe.assignment.question.Question;
import org.example.elearningbe.common.BaseEntity;

@Entity
@Table(name = "answers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "choice_id", nullable = false)
    private Choice choice;
}

