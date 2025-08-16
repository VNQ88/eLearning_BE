package org.example.elearningbe.course;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.common.PageResponse;
import org.example.elearningbe.common.respone.ResponseData;
import org.example.elearningbe.common.respone.ResponseError;
import org.example.elearningbe.course.dto.CourseRequest;
import org.example.elearningbe.course.dto.CourseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
@Slf4j
@Tag(name = "User Controller")
@RequiredArgsConstructor
public class CourseController {
    private  final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public ResponseData<?> createCourse(@Valid @RequestBody CourseRequest request) {
        log.info("Request to create course with title: {}", request.getTitle());
        try {
            CourseResponse courseResponse = courseService.createCourse(request);
            return new ResponseData<>(200, "Course created successfully", courseResponse);
        } catch (Exception e) {
            log.error("Error creating course: {}", e.getMessage(), e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "Get all courses", description = "Send a request via this API to get all courses")
    public ResponseData<?> getAllCourses(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                         @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize) {
        log.info("Request to get all courses");
        try {
            // Assuming there's a method in CourseService to get all courses
            PageResponse<?> courses = courseService.getAllCourses(pageNo, pageSize);
            return new ResponseData<>(HttpStatus.OK.value(), "courses", courses);
        } catch (Exception e) {
            log.error("Error retrieving courses: {}", e.getMessage(), e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Get course detail", description = "Send a request via this API to get course detail by courseId")
    public ResponseData<?> getCourse(@PathVariable @Min(1) long courseId) {
        log.info("Request to get course detail, courseId={}", courseId);
        try {
            CourseResponse courseResponse = courseService.getCourse(courseId);
            return new ResponseData<>(HttpStatus.OK.value(), "course", courseResponse);
        } catch (Exception e) {
            log.error("Error retrieving course: {}", e.getMessage(), e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/title")
    @Operation(summary = "Get course by title", description = "Send a request via this API to get course by title")
    public ResponseData<?> getCourseByTitle(@RequestParam String title) {
        log.info("Request to get course by title: {}", title);
        try {
            List<CourseResponse> courseResponses = courseService.getCourseByTitle(title);
            return new ResponseData<>(HttpStatus.OK.value(), "course", courseResponses);
        } catch (Exception e) {
            log.error("Error retrieving course by title: {}", e.getMessage(), e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

//    @PutMapping("/{courseId}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
//    @Operation(summary = "Update course", description = "Send a request via this API to update course by courseId")
//    public ResponseData<?> updateCourse(@PathVariable @Min(1) long courseId,
//                                        @RequestBody CourseCreationRequest request) {
}
