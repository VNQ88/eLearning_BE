package org.example.elearningbe.assignment.quiz_attempt.dto.response;

import org.example.elearningbe.assignment.quiz_attempt.dto.AnswerRequest;

import java.util.List;

public record SubmitAttemptRequest(
        List<AnswerRequest> answers
) {}
