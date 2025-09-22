package org.example.elearningbe.assignment.quiz;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.elearningbe.assignment.quiz.dto.QuizRequest;
import org.example.elearningbe.common.respone.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quiz Controller", description = "Operations related to quizzes")
public class QuizController {

    private final QuizService quizService;

    // ✅ Tạo quiz
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @PostMapping
    @Operation(summary = "Create a new quiz")
    public ResponseData<?> createQuiz(@RequestBody QuizRequest request) {
        return new ResponseData<>(HttpStatus.OK.value(), "Quiz created successfully", quizService.createQuiz(request));
    }

    // ✅ Cập nhật quiz
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @PutMapping("/{quizId}")
    @Operation(summary = "Update quiz")
    public ResponseData<?> updateQuiz(@PathVariable Long quizId,
                                                   @RequestBody QuizRequest request) {
        return new ResponseData<>(HttpStatus.OK.value(), "Quiz updated successfully", quizService.updateQuiz(quizId, request));
    }

    // ✅ Xoá quiz
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @DeleteMapping("/{quizId}")
    @Operation(summary = "Delete quiz")
    public ResponseData<?> deleteQuiz(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return new ResponseData<>(HttpStatus.OK.value(), "Quiz deleted successfully", null);
    }

    // ✅ Lấy chi tiết quiz
    @GetMapping("/{quizId}")
    @Operation(summary = "Get quiz detail")
    public ResponseData<?> getQuiz(@PathVariable Long quizId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Quiz details", quizService.getQuiz(quizId));
    }

    // ✅ Lấy danh sách quiz theo lesson
    @GetMapping("/lesson/{lessonId}")
    @Operation(summary = "Get quizzes by lesson")
    public ResponseData<?> getQuizzesByLesson(@PathVariable Long lessonId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Quizzes for lesson", quizService.getQuizzesByLesson(lessonId));
    }
}
