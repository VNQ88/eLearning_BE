package org.example.elearningbe.assignment.quiz_attempt.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AttemptAdminResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long attemptId;
    private Long quizId;
    private Long userId;
    private String userName;
    private Integer score;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}

