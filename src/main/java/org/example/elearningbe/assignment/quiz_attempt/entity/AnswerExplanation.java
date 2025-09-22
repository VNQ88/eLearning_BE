package org.example.elearningbe.assignment.quiz_attempt.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.common.BaseEntity;

@Entity
@Table(name = "answer_explanations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerExplanation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    @Column(columnDefinition = "TEXT")
    private String reasoning; // Giải thích chính

    @Column(columnDefinition = "TEXT")
    private String tip;       // Gợi ý kiến thức cần nhớ
}

