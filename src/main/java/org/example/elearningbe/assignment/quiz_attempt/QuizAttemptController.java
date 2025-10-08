package org.example.elearningbe.assignment.quiz_attempt;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.elearningbe.assignment.quiz_attempt.dto.response.SubmitAttemptRequest;
import org.example.elearningbe.common.respone.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quiz-attempts")
@RequiredArgsConstructor
@Tag(name = "Quiz Attempt Controller", description = "Operations related to quiz attempts")
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    // ✅ Bắt đầu attempt mới
    @GetMapping("/start")
    @Operation(summary = "Start a new quiz attempt and return questions")
    public ResponseData<?> startAttempt(@RequestParam Long quizId) {
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Quiz attempt started successfully",
                quizAttemptService.startAttempt(quizId)
        );
    }

    @PostMapping("/{attemptId}/submit")
    @Operation(summary = "Submit quiz attempt")
    public ResponseData<?> submitAttempt(
            @PathVariable Long attemptId,
            @RequestBody SubmitAttemptRequest request) {
        quizAttemptService.submitAttempt(attemptId, request);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Quiz submitted successfully"
        );
    }

    // ✅ Lấy kết quả cơ bản (user chỉ biết đúng/sai)
    @GetMapping("/{attemptId}")
    @Operation(summary = "Get attempt result (without showing correct answers)")
    public ResponseData<?> getAttempt(@PathVariable Long attemptId) {
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Quiz attempt result",
                quizAttemptService.getAttemptResult(attemptId) // trả về QuizResultResponse<AnswerResponse>
        );
    }

    // ✅ Lấy kết quả kèm đáp án đúng (chế độ review)
    @GetMapping("/{attemptId}/review")
    @Operation(summary = "Get attempt review (include correct answers)")
    public ResponseData<?> reviewAttempt(@PathVariable Long attemptId) {
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Quiz attempt review result",
                quizAttemptService.getAttemptReview(attemptId) // trả về QuizResultResponse<ReviewAnswerResponse>
        );
    }

    @GetMapping("/my")
    @Operation(summary = "Get my quiz attempts history by quiz")
    public ResponseData<?> getMyAttempts(
            @RequestParam Long quizId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "My quiz attempts",
                quizAttemptService.getMyAttempts(quizId, pageNo, pageSize)
        );
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get all quiz attempts for a quiz (admin/teacher only)")
    public ResponseData<?> getAllAttemptsByQuiz(
            @PathVariable Long quizId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "All attempts for quiz",
                quizAttemptService.getAllAttemptsByQuiz(quizId, pageNo, pageSize)
        );
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @DeleteMapping("/{attemptId}")
    @Operation(summary = "Delete a quiz attempt (admin/teacher only)")
    public ResponseData<?> deleteAttempt(@PathVariable Long attemptId) {
        quizAttemptService.deleteAttempt(attemptId);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Quiz attempt deleted successfully",
                null
        );
    }

    // ✅ Lấy lời giải thích AI cho 1 câu hỏi trong attempt
    @GetMapping("/{attemptId}/questions/{questionId}/explanation")
    @Operation(summary = "Get AI explanation for one question in an attempt")
    public ResponseData<?> getExplanationForAnswer(
            @PathVariable Long attemptId,
            @PathVariable Long questionId) {
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "AI explanation for one question",
                quizAttemptService.getExplanationForAnswer(attemptId, questionId)
        );
    }

    // ✅ Lấy lời giải thích AI cho tất cả câu hỏi trong attempt
    @GetMapping("/{attemptId}/explanations")
    @Operation(summary = "Get AI explanations for all questions in an attempt")
    public ResponseData<?> getExplanationsForAttempt(@PathVariable Long attemptId) {
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "AI explanations for all questions in the attempt",
                quizAttemptService.getExplanationsForAttempt(attemptId)
        );
    }

}
