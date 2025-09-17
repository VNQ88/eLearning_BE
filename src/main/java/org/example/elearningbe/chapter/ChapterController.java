// src/main/java/org/example/elearningbe/chapter/ChapterController.java
package org.example.elearningbe.chapter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.elearningbe.chapter.dto.*;
import org.example.elearningbe.common.respone.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("chapter")
@RequiredArgsConstructor
@Tag(name = "Chapter Controller")
public class ChapterController {
    private final ChapterService chapterService;

    @PostMapping()
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @Operation(summary = "Create chapter")
    public ResponseData<?> create(@Valid @RequestBody ChapterCreateRequest req) {
        return new ResponseData<>(HttpStatus.OK.value(), "created", chapterService.create(req));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "List chapters by course")
    public ResponseData<?> listByCourse(@PathVariable @Min(1) Long courseId) {
        List<ChapterResponse> items = chapterService.listByCourse(courseId);
        return new ResponseData<>(HttpStatus.OK.value(), "chapters", items);
    }

    @GetMapping("/{chapterId}")
    @Operation(summary = "Get chapter detail")
    public ResponseData<?> get(@PathVariable @Min(1) Long chapterId) {
        return new ResponseData<>(HttpStatus.OK.value(), "chapter", chapterService.get(chapterId));
    }

    @PutMapping("/{chapterId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @Operation(summary = "Update chapter")
    public ResponseData<?> update(@PathVariable @Min(1) Long chapterId,
                                  @Valid @RequestBody ChapterUpdateRequest req) {
        return new ResponseData<>(HttpStatus.OK.value(), "updated", chapterService.update(chapterId, req));
    }

    @DeleteMapping("/{chapterId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @Operation(summary = "Delete chapter")
    public ResponseData<?> delete(@PathVariable @Min(1) Long chapterId) {
        chapterService.delete(chapterId);
        return new ResponseData<>(HttpStatus.OK.value(), "deleted", null);
    }

    @PatchMapping("/{chapterId}/reorder")
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @Operation(summary = "Reorder chapter (change orderIndex)")
    public ResponseData<?> reorder(@PathVariable @Min(1) Long chapterId,
                                   @RequestParam @Min(0) Integer newOrderIndex) {
        return new ResponseData<>(HttpStatus.OK.value(), "reordered", chapterService.reorder(chapterId, newOrderIndex));
    }

    @PatchMapping("/courses/{courseId}/reindex")
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @Operation(summary = "Reindex nhiều chapter trong một course")
    public ResponseData<?> reindexBulk(
            @PathVariable Long courseId,
            @RequestBody @Valid List<ChapterReindexRequest> requests) {

        List<ChapterResponse> updated = chapterService.reindexBulk(courseId, requests);
        return new ResponseData<>(HttpStatus.OK.value(), "Reindex success", updated);
    }

}
