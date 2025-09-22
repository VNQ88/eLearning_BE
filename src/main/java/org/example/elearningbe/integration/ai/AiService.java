package org.example.elearningbe.integration.ai;

import org.example.elearningbe.assignment.quiz_attempt.dto.response.ExplanationDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {
    private final ChatClient chatClient;

    public AiService(ChatClient.Builder builder) {
        chatClient = builder.build();
    }

    public ExplanationDto explain(String question, String chosenAnswer, String correctAnswer) {
        String prompt = """
            Câu hỏi: %s
            Câu trả lời học viên: %s
            Đáp án đúng: %s

            Hãy trả lời ngắn gọn bằng tiếng Việt với JSON với 2 field:
            - reasoning: Giải thích tại sao đúng/sai
            - tip: Gợi ý kiến thức cần nhớ
            """.formatted(question, chosenAnswer, correctAnswer);

        return chatClient.prompt(prompt)
                .call()
                .entity(ExplanationDto.class); // parse thẳng ra DTO
    }
}
