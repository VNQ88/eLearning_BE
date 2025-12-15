package org.example.elearningbe.assignment.question.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CreateQuestionRequest implements Serializable {
    private String content;
    private String type; // MCQ, TRUE_FALSE
    private List<ChoiceRequest> choices;
}
