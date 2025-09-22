package org.example.elearningbe.assignment.quiz_attempt.dto.response;

import lombok.Builder;

@Builder
public record AnswerExplanationResponse(
        Long questionId,
        Long choiceId,
        Boolean isCorrect,
        String reasoning,
        String tip
) {}
