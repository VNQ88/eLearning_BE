package org.example.elearningbe.assignment.quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Data
public class QuizResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private Integer timeLimitSeconds;
    private Integer passScore;
    private Long lessonId;
}

