package org.example.elearningbe.assignment.question.dto;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class QuestionRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long quizId;
    private String content;
    private String type; // MCQ, TRUE_FALSE
    private List<ChoiceRequest> choices;
}

