package org.example.elearningbe.course_tracking.lesson_progress;
import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class UpdateLessonProgressRequest {
    @NotNull
    private Long lessonId;

    @Min(0)
    @Max(100)
    private Double progressPercent;

    @NotNull
    private String status; // IN_PROGRESS hoặc COMPLETED
}

