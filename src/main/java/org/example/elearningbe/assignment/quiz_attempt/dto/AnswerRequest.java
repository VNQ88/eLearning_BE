package org.example.elearningbe.assignment.quiz_attempt.dto;
public record AnswerRequest(
        Long questionId,
        Long choiceId
) {}
