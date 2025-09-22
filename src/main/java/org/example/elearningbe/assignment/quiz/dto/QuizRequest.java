package org.example.elearningbe.assignment.quiz.dto;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class QuizRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String title;
    private Integer timeLimitSeconds;
    private Integer passScore;
    private Long lessonId;
}

