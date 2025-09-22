package org.example.elearningbe.assignment.quiz_attempt.dto.response;

import org.example.elearningbe.assignment.quiz.dto.QuizResponse;

import java.time.LocalDateTime;
import java.util.List;

public record QuizResultResponse<T>(
        Long attemptId,
        QuizResponse quiz,
        Long userId,
        Integer score,
        LocalDateTime startedAt,
        LocalDateTime submittedAt,
        List<T> answers
) {}
