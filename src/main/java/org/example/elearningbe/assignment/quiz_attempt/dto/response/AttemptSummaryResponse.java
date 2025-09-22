package org.example.elearningbe.assignment.quiz_attempt.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptSummaryResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long attemptId;
    private Long quizId;
    private Integer score;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}

