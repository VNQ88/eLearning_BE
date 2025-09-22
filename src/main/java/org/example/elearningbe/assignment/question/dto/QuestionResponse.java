package org.example.elearningbe.assignment.question.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class QuestionResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String content;
    private String type;
    private Long quizId;
    private List<ChoiceResponse> choices;
}

