package org.example.elearningbe.course_tracking.course_enrollment;

import lombok.Builder;
import lombok.Data;
import org.example.elearningbe.course_tracking.lesson_progress.LessonProgressResponse;

import java.util.List;

@Data
@Builder
public class EnrollmentDetailResponse {
    private EnrollmentResponse enrollment;
    private List<LessonProgressResponse> lessons;
}

