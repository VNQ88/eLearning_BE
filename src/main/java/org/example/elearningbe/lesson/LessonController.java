package org.example.elearningbe.lesson;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.elearningbe.common.enumerate.LessonType;
import org.example.elearningbe.common.respone.ResponseData;
import org.example.elearningbe.lesson.dto.LessonCreateRequest;
import org.example.elearningbe.lesson.dto.LessonPatchRequest;
import org.example.elearningbe.lesson.dto.LessonUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lessons")
@Tag(name = "Lesson Controller")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    @Operation(summary = "Create a new lesson", description = "Create a new lesson under a specific course and optionally a chapter")
    public ResponseData<?> createLesson(@Valid @RequestBody LessonCreateRequest req) throws Exception {
        return new ResponseData<>(HttpStatus.OK.value(), "Lesson created successfully", lessonService.createLesson(req));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lesson details", description = "Retrieve detailed information about a specific lesson by its ID")
    public ResponseData<?> getLesson(@PathVariable Long id) throws Exception {
        return new ResponseData<>(HttpStatus.OK.value(), "Lesson details", lessonService.getLesson(id));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a lesson", description = "Update the details of an existing lesson by its ID")
    public ResponseData<?> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody LessonUpdateRequest req
    ) throws Exception {
        return new ResponseData<>(HttpStatus.OK.value(), "Lesson updated successfully", lessonService.updateLesson(id, req));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    @PatchMapping("/{id}")
    @Operation(summary = "Patch a lesson", description = "Partially update the details of an existing lesson by its ID")
    public ResponseData<?> patchLesson(
            @PathVariable Long id,
            @RequestBody LessonPatchRequest req
    ) throws Exception {
        return new ResponseData<>(HttpStatus.OK.value(), "Lesson patched successfully", lessonService.patchLesson(id, req));
    }

    @GetMapping
    @Operation(summary = "Get lessons with filters and pagination",
            description = "Retrieve lessons with optional filters: courseId, chapterId, type, title")
    public ResponseData<?> getLessons(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) LessonType type,
            @RequestParam(required = false) String title
    ) {
        return new ResponseData<>(HttpStatus.OK.value(), "Lesson list",
                lessonService.getLessons(pageNo, pageSize, courseId, chapterId, type, title));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a lesson", description = "Delete a lesson by ID and remove its resources from MinIO")
    public ResponseData<?> deleteLesson(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        lessonService.deleteLesson(id, username);
        return new ResponseData<>(HttpStatus.OK.value(), "Lesson deleted successfully", null);
    }
}
