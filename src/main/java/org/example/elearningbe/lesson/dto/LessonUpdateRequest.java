package org.example.elearningbe.lesson.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.elearningbe.common.enumerate.LessonType;

@Data
public class LessonUpdateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    // nếu null -> giữ nguyên
    private String imageObjectKey;

    // nếu null -> giữ nguyên
    private String resourceObjectKey;

    @NotNull(message = "Lesson type is required")
    private LessonType type;

    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration must be >= 0")
    private Integer durationMinutes;

    @PositiveOrZero(message = "Order index must be >= 0")
    private Integer orderIndex;

    private Long chapterId;
}
