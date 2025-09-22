package org.example.elearningbe.assignment.question;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.elearningbe.assignment.question.dto.QuestionRequest;
import org.example.elearningbe.common.respone.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "Question Controller", description = "Operations related to questions and choices")
public class QuestionController {

    private final QuestionService questionService;

    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @PostMapping
    @Operation(summary = "Create a new question with choices")
    public ResponseData<?> createQuestion(@RequestBody QuestionRequest request) {
        return new ResponseData<>(HttpStatus.OK.value(),
                "Question created successfully",
                questionService.createQuestion(request));
    }

    // ✅ Lấy chi tiết question kèm choices
    @GetMapping("/{questionId}")
    @Operation(summary = "Get question details including choices")
    public ResponseData<?> getQuestion(@PathVariable Long questionId) {
        return new ResponseData<>(HttpStatus.OK.value(),
                "Question details",
                questionService.getQuestion(questionId));
    }

    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get questions by quiz (paginated)")
    public ResponseData<?> getQuestionsByQuiz(@PathVariable Long quizId,
                                              @RequestParam(defaultValue = "0") int pageNo,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return new ResponseData<>(HttpStatus.OK.value(),
                "Questions for quiz",
                questionService.getQuestionsByQuiz(quizId, pageNo, pageSize));
    }

    // ✅ Cập nhật question kèm choices
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @PutMapping("/{questionId}")
    @Operation(summary = "Update a question with choices")
    public ResponseData<?> updateQuestion(@PathVariable Long questionId,
                                          @RequestBody QuestionRequest request) {
        return new ResponseData<>(HttpStatus.OK.value(),
                "Question updated successfully",
                questionService.updateQuestion(questionId, request));
    }

    // ✅ Xoá question (xoá luôn choices)
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @DeleteMapping("/{questionId}")
    @Operation(summary = "Delete a question and its choices")
    public ResponseData<?> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return new ResponseData<>(HttpStatus.OK.value(),
                "Question deleted successfully",
                null);
    }
}

