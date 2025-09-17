package org.example.elearningbe.chapter.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class ChapterUpdateRequest {
    @NotBlank
    private String title;

    @Nullable
    @PositiveOrZero
    private Integer orderIndex; // optional; map -> entity.index
}
