package org.example.elearningbe.assignment.quiz_attempt.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long questionId;   // câu hỏi
    private Long choiceId;     // đáp án mà user chọn
    private Boolean isCorrect; // đúng/sai
}

