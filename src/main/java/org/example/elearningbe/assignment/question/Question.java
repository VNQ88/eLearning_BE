package org.example.elearningbe.assignment.question;


import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.assignment.quiz.Quiz;
import org.example.elearningbe.common.BaseEntity;

import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question extends BaseEntity {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 20)
    private String type; // MCQ, TRUE_FALSE,...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Choice> choices;
}
