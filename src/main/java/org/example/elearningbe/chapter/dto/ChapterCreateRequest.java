package org.example.elearningbe.chapter.dto;

import jakarta.validation.constraints.*;

import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class ChapterCreateRequest {
    @NotNull
    private Long courseId;

    @NotBlank
    private String title;

    @Nullable
    @PositiveOrZero
    private Integer orderIndex; // map -> entity.index
}
