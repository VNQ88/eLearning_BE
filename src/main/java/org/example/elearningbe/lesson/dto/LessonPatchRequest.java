package org.example.elearningbe.lesson.dto;


import lombok.Data;
import org.example.elearningbe.common.enumerate.LessonType;

@Data
public class LessonPatchRequest {
    private String title;
    private String description;
    private String imageObjectKey;
    private String resourceObjectKey;
    private LessonType type;
    private Integer durationMinutes;
    private Integer orderIndex;
    private Long chapterId;
}

