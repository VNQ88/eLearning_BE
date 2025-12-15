package org.example.elearningbe.assignment.question.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AddQuestionsRequest implements Serializable {
    private Long quizId;                    // quiz đã tồn tại
    private List<CreateQuestionRequest> questions; // danh sách câu hỏi muốn thêm vào
}

