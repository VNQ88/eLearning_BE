package org.example.elearningbe.assignment.question;


import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.common.BaseEntity;

@Entity
@Table(name = "choices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Choice extends BaseEntity {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}

