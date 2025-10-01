package org.example.elearningbe.course_tracking.course_enrollment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.common.respone.ResponseData;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
@Slf4j
@Tag(name = "Course Enrollment Controller")
@RequiredArgsConstructor
public class CourseEnrollmentController {
    private final CourseEnrollmentService enrollmentService;

    @GetMapping("/my")
    @Operation(summary = "Get my enrollments")
    public ResponseData<List<EnrollmentResponse>> getMyEnrollments() {
        log.info("Request to get my enrollments");
        return new ResponseData<>(HttpStatus.OK.value(), "My enrollments", enrollmentService.getMyEnrollments());
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Get enrollment detail for a course")
    public ResponseData<EnrollmentDetailResponse> getEnrollmentDetail(@PathVariable Long courseId) {
        log.info("Request to get enrollment detail for course {}", courseId);
        return new ResponseData<>(HttpStatus.OK.value(), "Enrollment detail",
                enrollmentService.getEnrollmentDetail(courseId));
    }
}

