package org.example.elearningbe.assignment.question.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@Data
public class ChoiceResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String content;
    private Boolean isCorrect;
}

