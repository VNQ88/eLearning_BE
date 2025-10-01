package org.example.elearningbe.course_tracking.lesson_progress;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LessonProgressResponse {
    private Long lessonProgressId;
    private Long lessonId;
    private String lessonTitle;
    private String status;           // ProgressStatus (NOT_STARTED, IN_PROGRESS, COMPLETED)
    private Double progressPercent;
    private LocalDateTime lastAccessTime;
}

