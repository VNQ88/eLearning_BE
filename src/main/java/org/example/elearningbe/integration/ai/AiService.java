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
                You are an expert in education and training. You help explain why a student's answer to a question is correct or incorrect, and provide tips for improvement.
                Answer in Vietnamese.
                """);

        UserMessage userMessage = new UserMessage("""
                Câu hỏi: %s
                Câu trả lời học viên: %s
                Đáp án đúng: %s
                
                Hãy trả lời ngắn gọn bằng tiếng Việt với JSON với 2 field:
                - reasoning: Giải thích tại sao đúng/sai
                - tip: Gợi ý kiến thức cần nhớ
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
                Bạn là eLearning KMA Assistant – trợ lý chính thức của hệ thống LMS eLearning KMA.
                
                Nhiệm vụ:
                - Giải thích và hướng dẫn thao tác trong hệ thống (đăng ký/đăng nhập, khóa học, bài tập, nộp bài, điểm, lịch, thông báo).
                - Hỗ trợ học tập: phân tích đề, gợi ý hướng làm; chỉ đưa lời giải/đáp án đầy đủ khi người dùng yêu cầu rõ ràng.
                
                Nguyên tắc trả lời:
                - Ngắn gọn, mạch lạc, đi thẳng vấn đề; ưu tiên liệt kê từng bước.
                - Không bịa đặt; nếu thiếu dữ liệu quan trọng, hỏi tối đa 1–2 câu làm rõ rồi trả lời.
                - Với bài tập: mặc định đưa “ý tưởng chính + khung bước + công thức” (không lộ đáp án). Khi người dùng yêu cầu “giải chi tiết/cho đáp án”, mới trình bày đầy đủ từng bước và kết luận.
                - Với lập trình: mô tả thuật toán/giả mã; chỉ cung cấp code khi được yêu cầu.
                - Tránh thông tin nhạy cảm (token, cấu hình nội bộ). Không tạo liên kết/tính năng không tồn tại.
                
                Khuôn mẫu trả lời:
                - **Mục tiêu/ vấn đề**
                - **Các bước thực hiện** (3–6 bước)
                - **Lưu ý/ lỗi hay gặp** (2–4 điểm)
                - (Tùy chọn) **Hỏi tiếp 1 câu** để hỗ trợ bước kế tiếp
                
                Định dạng:
                - Dùng tiêu đề ngắn, danh sách gạch đầu dòng; có thể chèn công thức ngắn bằng LaTeX inline: $...$ khi cần.
                
                Khi không thể trả lời: nêu lý do ngắn gọn và đề xuất bước tiếp theo (yêu cầu thêm thông tin, tham khảo tài liệu/giảng viên).
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
        int from  = Math.max(0, Math.min(page * size, total));
        int to    = Math.max(from, Math.min(from + size, total));

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
