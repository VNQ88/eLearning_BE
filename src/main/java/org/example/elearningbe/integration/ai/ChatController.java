package org.example.elearningbe.integration.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.common.AppUtils;
import org.example.elearningbe.common.respone.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Controller")
@Slf4j
public class ChatController {
    private final AiService aiService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Chat with chatbot Gemini (can upload image/pdf/txt file)",
        description = "Send a message to the chatbot and receive a response. You can also upload image/pdf/txt files to provide additional context for the conversation. The files will not be stored permanently."
    )
    public ResponseData<?> chat(
            @RequestPart("message") String message,
            @RequestPart(value="files", required=false) List<MultipartFile> files)
    {
        String userEmail = AppUtils.getCurrentUserEmail();
        return new ResponseData<>(HttpStatus.OK.value(), "Chat response", aiService.chat(userEmail, message, files));
    }

    @GetMapping("/history")
    public  ResponseData<?> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String userEmail = AppUtils.getCurrentUserEmail();
        return new ResponseData<>(HttpStatus.OK.value(), "Chat history",
                aiService.getHistory(userEmail, page, size));
    }

    @DeleteMapping
    @Operation(
        summary = "Clear chat history",
        description = "Clear the entire chat history for the current user."
    )
    public ResponseData<?> clearHistory() {
        String userEmail = AppUtils.getCurrentUserEmail();
        aiService.clearHistory(userEmail);
        return new ResponseData<>(HttpStatus.OK.value(), "Chat history cleared");
    }

}
