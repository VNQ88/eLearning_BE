package org.example.elearningbe.lesson.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.elearningbe.common.enumerate.LessonType;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonResponse implements Serializable {
    private Long id;
    private String title;
    private String description;

    private String imageUrl;      // presigned GET
    private String resourceUrl;   // presigned GET cho video/doc

    private LessonType type;

    private Integer orderIndex;
    private Long courseId;
    private Long chapterId;
    private Integer durationMinutes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

