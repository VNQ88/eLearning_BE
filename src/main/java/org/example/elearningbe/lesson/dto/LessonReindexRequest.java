package org.example.elearningbe.lesson.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonReindexRequest {
    @NotNull
    private Long lessonId;

    @NotNull
    @Min(0)
    private Integer orderIndex;
}

