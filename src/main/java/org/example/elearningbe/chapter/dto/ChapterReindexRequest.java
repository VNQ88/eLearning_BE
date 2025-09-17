package org.example.elearningbe.chapter.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChapterReindexRequest {
    @NotNull
    private Long chapterId;

    @NotNull
    @Min(0)
    private Integer orderIndex;
}
