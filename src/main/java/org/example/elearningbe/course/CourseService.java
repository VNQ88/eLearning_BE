package org.example.elearningbe.course;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.common.PageResponse;
import org.example.elearningbe.course.dto.CourseRequest;
import org.example.elearningbe.course.dto.CourseResponse;
import org.example.elearningbe.course.entities.Course;
import org.example.elearningbe.exception.ResourceNotFoundException;
import org.example.elearningbe.user.UserRepository;
import org.example.elearningbe.user.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseResponse createCourse(CourseRequest request) {
        log.info("Request to create course with title: {}", request.toString());
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .image(request.getImage())
                .price(request.getPrice())
                .duration(request.getDuration())
                .build();
        Optional<User> owner;
        if (request.getOwnerEmail() != null) {
            owner = userRepository.findByEmail(request.getOwnerEmail());
            if (owner.isEmpty()) {
                throw new ResourceNotFoundException("Owner not found with email: " + request.getOwnerEmail());
            }
            course.setOwner(owner.get());
        }
        else {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            owner = userRepository.findByEmail(userEmail);
            if (owner.isEmpty()) {
                throw new ResourceNotFoundException("Current user not found");
            }

        }
        course.setOwner(owner.get());
        courseRepository.save(course);
        return mapToCourseResponse(course);
    }

    public PageResponse<?> getAllCourses(int pageNo, @Min(10) int pageSize) {
        Page<Course> coursePage = courseRepository.findAll(PageRequest.of(pageNo, pageSize));
        List<CourseResponse> courseResponses = coursePage.getContent().stream()
                .map(this::mapToCourseResponse)
                .toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(coursePage.getTotalPages())
                .items(courseResponses)
                .build();
    }

    public CourseResponse getCourse(@Min(1) long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        return mapToCourseResponse(course);
    }

    private CourseResponse mapToCourseResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getImage(),
                course.getPrice(),
                course.getDuration(),
                course.getOwner().getEmail());
    }

    public Course mapToCourse(CourseRequest request) {
        return Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .image(request.getImage())
                .price(request.getPrice())
                .duration(request.getDuration())
                .build();
    }

    public List<CourseResponse> getCourseByTitle(String title) {
        List<Course> courses = courseRepository.findByTitleContainingIgnoreCase(title);
        return courses.stream().map(this::mapToCourseResponse).collect(Collectors.toList());
    }

    public CourseResponse updateCourse(@Min(1) long courseId, @Valid CourseRequest request) {
        log.info("Request to update course with id: {}, title: {}", courseId, request.getTitle());
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        Optional<User> owner;
        if (request.getOwnerEmail() != null && !request.getOwnerEmail().isEmpty()) {
            owner = userRepository.findByEmail(request.getOwnerEmail());
            if (owner.isEmpty()) {
                throw new ResourceNotFoundException("Owner not found with email: " + request.getOwnerEmail());
            }
            course.setOwner(owner.get());
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setImage(request.getImage());
        course.setPrice(request.getPrice());
        course.setDuration(request.getDuration());
        courseRepository.save(course);
        return mapToCourseResponse(course);
    }
}
