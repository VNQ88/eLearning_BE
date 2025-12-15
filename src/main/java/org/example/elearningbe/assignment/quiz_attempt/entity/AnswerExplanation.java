package org.example.elearningbe.assignment.quiz_attempt.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.common.BaseEntity;
import org.example.elearningbe.security.AesEncryptor;

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

    @Convert(converter = AesEncryptor.class)
    @Column(columnDefinition = "TEXT")
    private String reasoning; // Giải thích chính

    @Convert(converter = AesEncryptor.class)
    @Column(columnDefinition = "TEXT")
    private String tip;       // Gợi ý kiến thức cần nhớ
}

