package org.example.elearningbe.lesson.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.elearningbe.common.enumerate.LessonType;

import java.io.Serializable;

@Data
public class LessonCreateRequest implements Serializable {
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    // optional
    private String imageObjectKey;

    @NotBlank(message = "Resource object key is required")
    private String resourceObjectKey; // MinIO objectKey

    @NotNull(message = "Lesson type is required")
    private LessonType type;  // VIDEO / DOCUMENT

    @NotNull(message = "CourseId is required")
    private Long courseId;

    private Long chapterId;

    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration must be >= 0")
    private Integer durationMinutes;

    @PositiveOrZero(message = "Order index must be >= 0")
    private Integer orderIndex;
}
