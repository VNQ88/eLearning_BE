package org.example.elearningbe.course_tracking.lesson_progress;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.common.respone.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lesson-progress")
@Slf4j
@Tag(name = "Lesson Progress Controller")
@RequiredArgsConstructor
public class LessonProgressController {
    private final LessonProgressService lessonProgressService;

    @GetMapping("/{enrollmentId}/{lessonId}")
    @Operation(summary = "Get lesson progress")
    public ResponseData<LessonProgressResponse> getLessonProgress(@PathVariable Long enrollmentId,
                                                                  @PathVariable Long lessonId) {
        log.info("Get lesson progress: enrollment={}, lesson={}", enrollmentId, lessonId);
        return new ResponseData<>(HttpStatus.OK.value(), "Lesson progress",
                lessonProgressService.getLessonProgress(enrollmentId, lessonId));
    }

    @PutMapping("/{enrollmentId}/{lessonId}")
    @Operation(summary = "Update lesson progress")
    public ResponseData<LessonProgressResponse> updateLessonProgress(@PathVariable Long enrollmentId,
                                                                     @PathVariable Long lessonId,
                                                                     @RequestBody UpdateLessonProgressRequest request) {
        log.info("Update lesson progress: enrollment={}, lesson={}", enrollmentId, lessonId);
        return new ResponseData<>(HttpStatus.OK.value(), "Lesson progress updated",
                lessonProgressService.updateLessonProgress(enrollmentId, lessonId, request));
    }

    @PostMapping("/{enrollmentId}/{lessonId}/complete")
    @Operation(summary = "Mark lesson completed")
    public ResponseData<LessonProgressResponse> markLessonCompleted(@PathVariable Long enrollmentId,
                                                                    @PathVariable Long lessonId) {
        log.info("Mark lesson completed: enrollment={}, lesson={}", enrollmentId, lessonId);
        return new ResponseData<>(HttpStatus.OK.value(), "Lesson marked as completed",
                lessonProgressService.markLessonCompleted(enrollmentId, lessonId));
    }

}

