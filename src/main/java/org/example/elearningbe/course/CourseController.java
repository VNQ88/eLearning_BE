package org.example.elearningbe.course;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.common.PageResponse;
import org.example.elearningbe.common.respone.ResponseData;
import org.example.elearningbe.course.dto.CourseRequest;
import org.example.elearningbe.course.dto.CourseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
@Slf4j
@Tag(name = "Course Controller")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create new course", description = "Create course with image upload (multipart/form-data)")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public ResponseData<?> createCourse(@Valid @ModelAttribute CourseRequest request) throws Exception {
        log.info("Request to create course with title: {}", request.getTitle());
        CourseResponse courseResponse = courseService.createCourse(request);
        return new ResponseData<>(HttpStatus.OK.value(), "Course created successfully", courseResponse);
    }

    @GetMapping("/list")
    @Operation(summary = "Get all courses", description = "Get paginated courses")
    public ResponseData<?> getAllCourses(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                         @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize) {
        log.info("Request to get all courses");
        PageResponse<?> courses = courseService.getAllCourses(pageNo, pageSize);
        return new ResponseData<>(HttpStatus.OK.value(), "courses", courses);
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Get course detail", description = "Get course detail by courseId")
    public ResponseData<?> getCourse(@PathVariable @Min(1) long courseId) {
        log.info("Request to get course detail, courseId={}", courseId);
        CourseResponse courseResponse = courseService.getCourse(courseId);
        return new ResponseData<>(HttpStatus.OK.value(), "course", courseResponse);
    }

    @GetMapping("/title")
    @Operation(summary = "Get course by title", description = "Find courses by title (case-insensitive)")
    public ResponseData<?> getCourseByTitle(@RequestParam String title,
                                            @RequestParam (defaultValue = "0", required = false) Integer pageNo,
                                            @RequestParam(defaultValue = "20", required = false) @Min(10) Integer pageSize) {
        log.info("Request to get course by title: {}", title);
        return new ResponseData<>(HttpStatus.OK.value(), "course", courseService.getCourseByTitle(title, pageNo, pageSize));
    }



    @GetMapping
    @Operation(summary = "Get courses with filters")
    public ResponseData<?> getCoursesWithFilter(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String ownerEmail,
            @RequestParam(required = false) Float minPrice,
            @RequestParam(required = false) Float maxPrice
    ) {
        return new ResponseData<>(HttpStatus.OK.value(),
                "Filtered courses",
                courseService.getCoursesWithFilter(pageNo, pageSize, title, category, ownerEmail, minPrice, maxPrice));
    }


    @PutMapping(value = "/{courseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    @Operation(summary = "Update course", description = "Update course (optionally replace image) with multipart/form-data")
    public ResponseData<?> updateCourse(@PathVariable @Min(1) long courseId,
                                        @Valid @ModelAttribute CourseRequest request) throws Exception {
        log.info("Request to update course with id: {}, title: {}", courseId, request.getTitle());
        CourseResponse courseResponse = courseService.updateCourse(courseId, request);
        return new ResponseData<>(HttpStatus.OK.value(), "Course updated successfully", courseResponse);
    }

    @GetMapping("/my-courses")
    @Operation(summary = "Get purchased courses of current user (paginated)", description = "Get courses purchased by the current user with pagination")
    public ResponseData<PageResponse<List<CourseResponse>>> getMyCourses(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return new ResponseData<>(HttpStatus.OK.value(),
                "Purchased courses",
                courseService.getMyCourses(pageNo, pageSize));
    }
}