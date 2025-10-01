package org.example.elearningbe.course_tracking.course_enrollment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponse {
    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private String ownerEmail;
    private String status;           // EnrollmentStatus (ENROLLED, IN_PROGRESS, COMPLETED)
    private Double progressPercent;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}

