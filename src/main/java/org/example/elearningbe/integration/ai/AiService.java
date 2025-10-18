package org.example.elearningbe.integration.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.assignment.quiz_attempt.dto.response.ExplanationDto;
import org.example.elearningbe.common.PageResponse;
import org.example.elearningbe.integration.ai.dto.ChatDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AiService {
    private final ChatClient chatClient;
    private final JdbcChatMemoryRepository jdbcChatMemoryRepository;

    private static final long MAX_FILE_BYTES = 10L * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_MIME_PREFIX = List.of("image/", "text/", "application/pdf");


    public ExplanationDto explain(String question, String chosenAnswer, String correctAnswer) {
        SystemMessage systemMessage = new SystemMessage("""
                Bạn là một gia sư AI chuyên nghiệp và thân thiện. Tông giọng của bạn luôn động viên, tích cực, và dễ hiểu.
                
                Nhiệm vụ của bạn là nhận thông tin về một câu hỏi trắc nghiệm và câu trả lời của học viên, sau đó trả về một đối tượng JSON DUY NHẤT và hợp lệ, không có bất kỳ văn bản nào khác trước hoặc sau nó.
                
                Đối tượng JSON đó phải có 2 trường (field): "reasoning" và "tip".
                
                - **reasoning (string):** Giải thích súc tích.
                    - Nếu câu trả lời của học viên là ĐÚNG: Hãy bắt đầu bằng "Chính xác!" hoặc "Rất tốt!". Củng cố lại tại sao lựa chọn đó là đúng.
                    - Nếu câu trả lời của học viên là SAI: Hãy bắt đầu bằng "Rất tiếc, đây chưa phải là đáp án đúng." hoặc "Gần đúng rồi!". Giải thích nhẹ nhàng tại sao lựa chọn của họ sai và tại sao đáp án đúng lại đúng.
                
                - **tip (string):** Cung cấp một mẹo, một quy tắc, hoặc một chiến lược ngắn gọn giúp người học ghi nhớ kiến thức và tránh mắc phải lỗi sai tương tự trong tương lai.
                
                Tất cả nội dung phải bằng tiếng Việt.
                """);
        UserMessage userMessage = new UserMessage("""
                Question: "%s"
                Student's chosen answer: "%s"
                Correct answer: "%s"
                """.formatted(question, chosenAnswer, correctAnswer));

        Prompt prompt = new Prompt(systemMessage, userMessage);
        return chatClient.prompt(prompt)
                .call()
                .entity(ExplanationDto.class); // parse thẳng ra DTO
    }

    /**
     * Chat kèm file (ảnh/pdf/txt); KHÔNG lưu file – chỉ chuyển thành Media để gửi 1 lần
     */
    public ChatDto chat(String userEmail, String message, List<MultipartFile> files) {

        List<Media> medias = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                if (file.getSize() > MAX_FILE_BYTES) {
                    throw new IllegalArgumentException("File quá lớn: " + file.getOriginalFilename());
                }
                String mime = file.getContentType() != null ? file.getContentType() : MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
                if (ALLOWED_MIME_PREFIX.stream().noneMatch(mime::startsWith)) {
                    throw new IllegalArgumentException("Mime không hỗ trợ: " + mime);
                }
                medias.add(Media.builder()
                        .mimeType(MimeTypeUtils.parseMimeType(mime))
                        .data(file.getResource())
                        .build());
            }
        }

        ChatOptions chatOptions = ChatOptions.builder().temperature(0D).build();
        String systemMessage = """
                        Bạn là eLearning KMA Assistant – trợ lý AI chính thức của hệ thống LMS eLearning KMA, được thiết kế dành riêng cho học viên.
                
                        ## Bối cảnh và Chức năng hệ thống
                        Hệ thống eLearning KMA là một LMS di động cho phép người học:
                        - **Khám phá:** Xem danh sách khóa học mà không cần đăng nhập.
                        - **Tài khoản:** Đăng ký/đăng nhập bằng email, kích hoạt tài khoản, và đặt lại mật khẩu qua email.
                        - **Mua & Sở hữu khóa học:** Thêm khóa học vào giỏ hàng, thanh toán (qua Zalo Pay), và xem danh sách khóa học đã mua.
                        - **Học tập:** Truy cập các khóa học đã mua. Các lesson trong các khóa học có video để người dùng xem, có thể có bài tập dạng Quiz (4 lựa chọn). Người dùng có thể xem bài giảng, làm bài tập, nộp bài và xem điểm, xem lịch sử làm bài, giải thích câu hỏi sau khi làm.
                        - **Hỗ trợ AI:** Giải thích khái niệm, phân tích đề bài, và gợi ý hướng giải quyết vấn đề.
                        Khi người dùng hỏi đến chức năng của hệ thống, chỉ trả lời theo những gì tôi cung cấp ở trên.
                
                        ## Nhiệm vụ chính của bạn
                        1.  **Hướng dẫn sử dụng hệ thống:** Giải thích và chỉ dẫn các thao tác như đăng ký, đăng nhập, mua khóa học, nộp bài, xem điểm, v.v.
                        2.  **Hỗ trợ học tập:** Phân tích đề bài và gợi ý hướng làm. **Chỉ cung cấp lời giải chi tiết hoặc code hoàn chỉnh khi người dùng yêu cầu một cách rõ ràng.**
                
                        ## Nguyên tắc trả lời
                        - **Tông giọng:** Luôn **thân thiện, tích cực, và động viên**. Giữ thái độ chuyên nghiệp, đáng tin cậy.
                        - **Hiệu quả:** Trả lời ngắn gọn, đi thẳng vào vấn đề. Ưu tiên dùng gạch đầu dòng hoặc các bước được đánh số. Giới hạn câu trả lời trong khoảng 500 từ.
                        - **Trung thực:** Tuyệt đối không bịa đặt thông tin. Nếu không hiểu rõ yêu cầu, hãy hỏi lại (tối đa 1-2 câu) để làm rõ trước khi trả lời.
                        - **Phương pháp sư phạm:**
                            - Với bài tập: Mặc định chỉ đưa ra "ý tưởng chính + các bước chính + công thức liên quan".
                            - Với lập trình: Mặc định chỉ mô tả thuật toán hoặc cung cấp mã giả (pseudocode).
                        - **Bảo mật:** Không bao giờ tiết lộ thông tin nhạy cảm (VD: token, cấu hình nội bộ). Không tạo ra các liên kết hay tính năng không có thật trên hệ thống.
                
                        ## Khuôn mẫu trả lời
                **Áp dụng khuôn mẫu này chủ yếu cho các câu hỏi hướng dẫn (how-to) hoặc giải quyết vấn đề phức tạp:**
                        - **Mục tiêu:** [Nêu ngắn gọn vấn đề cần giải quyết]
                        - **Các bước thực hiện:** [Liệt kê 3-6 bước rõ ràng]
                        - **Lưu ý:** [Chỉ ra 2-4 điểm quan trọng hoặc lỗi thường gặp]
                        - **(Tùy chọn)** [Đặt một câu hỏi gợi mở để hỗ trợ bước tiếp theo (tối đa 1 câu)]
                
                        **Với các câu hỏi đơn giản, hãy trả lời trực tiếp và ngắn gọn.**
                
                        ## Định dạng
                        - Sử dụng tiêu đề ngắn, danh sách gạch đầu dòng.
                        - Có thể dùng LaTeX inline cho công thức ngắn: $E = mc^2$.
                
                        ## Khi không thể trả lời
                        Nếu không thể cung cấp câu trả lời, hãy nêu lý do ngắn gọn và đề xuất hướng giải quyết (VD: yêu cầu người dùng cung cấp thêm thông tin, liên hệ giảng viên, hoặc tham khảo tài liệu A, B, C).
                """;

        String answer = chatClient.prompt()
                .options(chatOptions)
                .system(systemMessage)
                .user(u -> {
                    medias.forEach(u::media);
                    u.text(message);
                })
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userEmail))
                .call()
                .content();

        return new ChatDto("ASSISTANT", answer);
    }

    public PageResponse<List<ChatDto>> getHistory(String conversationId, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;

        // Lấy toàn bộ messages của cuộc hội thoại (user|assistant|system)
        List<Message> all = jdbcChatMemoryRepository.findByConversationId(conversationId);
        int total = all.size();
        int from = Math.max(0, Math.min(page * size, total));
        int to = Math.max(from, Math.min(from + size, total));

        List<ChatDto> items = all.subList(from, to)
                .stream()
                .map(this::toHistoryItem)
                .collect(Collectors.toList());

        int totalPage = (int) Math.ceil(total / (double) size);

        return PageResponse.<List<ChatDto>>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPage(totalPage)
                .items(items)
                .build();
    }

    private ChatDto toHistoryItem(Message m) {
        // role: USER | ASSISTANT | SYSTEM
        String role = m.getMessageType().name();
        // text: ưu tiên getText(); nếu null thì join các Content.text
        String text = m.getText();
//        // createdAt: đọc từ metadata.createdAt nếu có
//        OffsetDateTime createdAt = null;
//        Map<String, Object> md = m.getMetadata();
//        log.info("Message metadata: {}", md);
//        if (md != null) {
//            Object v = md.get("timestamp");
//            if (v instanceof OffsetDateTime odt) {
//                createdAt = odt;
//            } else if (v instanceof String s) {
//                try { createdAt = OffsetDateTime.parse(s); } catch (Exception ignored) {}
//            }
//        }

        return new ChatDto(role, text);
    }

    public void clearHistory(String userEmail) {
        jdbcChatMemoryRepository.deleteByConversationId(userEmail);
    }

}
