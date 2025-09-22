package org.example.elearningbe.assignment.quiz_attempt.dto.response;

import org.example.elearningbe.assignment.question.dto.QuestionResponse;
import org.example.elearningbe.assignment.quiz.dto.QuizResponse;

import java.time.LocalDateTime;
import java.util.List;

public record StartAttemptResponse(
        Long attemptId,
        QuizResponse quiz,
        Long userId,
        LocalDateTime startedAt,
        List<QuestionResponse> questions
) {}
