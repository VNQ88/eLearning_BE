package org.example.elearningbe.course;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.common.PageResponse;
import org.example.elearningbe.common.enumerate.CourseCategory;
import org.example.elearningbe.course.dto.CourseRequest;
import org.example.elearningbe.course.dto.CourseResponse;
import org.example.elearningbe.course.entities.Course;
import org.example.elearningbe.exception.ResourceNotFoundException;
import org.example.elearningbe.integration.minio.MinioChannel;
import org.example.elearningbe.integration.minio.dto.UploadResult;
import org.example.elearningbe.user.UserRepository;
import org.example.elearningbe.user.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {
    private final MinioChannel minioChannel;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /* ================== CREATE ================== */
    public CourseResponse createCourse(@Valid CourseRequest request) throws Exception {
        log.info("Create course: {}", request.getTitle());
        Course course = mapToCourse(request);

        // Xác định owner
        User owner = resolveOwner(request.getOwnerEmail());
        course.setOwner(owner);

        // Upload ảnh -> lưu objectKey vào DB
        UploadResult result = minioChannel.upload(request.getImage()); // trả objectKey + presigned URL
        course.setImage(result.objectKey()); // CHỈ lưu objectKey

        courseRepository.save(course);
        return mapToCourseResponse(course); // convert objectKey -> presignedUrl khi trả ra
    }

    /* ================== LIST ================== */
    public PageResponse<?> getAllCourses(int pageNo, @Min(10) int pageSize) {
        Page<Course> page = courseRepository.findAll(PageRequest.of(pageNo, pageSize));
        List<CourseResponse> items = page.getContent().stream()
                .map(this::mapToCourseResponse)
                .toList();

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(items)
                .build();
    }

    /* ================== DETAIL ================== */
    public CourseResponse getCourse(@Min(1) long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        return mapToCourseResponse(course);
    }

    /* ================== SEARCH BY TITLE ================== */
    public PageResponse<List<CourseResponse>> getCourseByTitle(String title, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Course> page = courseRepository.findByTitleContainingIgnoreCase(title, pageable);
        List<CourseResponse> items = page.getContent().stream()
                .map(course -> {
                    try {
                        return mapToCourseResponse(course);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
        return PageResponse.<List<CourseResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(items)
                .build();
    }

    /* ================== UPDATE ================== */
    public CourseResponse updateCourse(@Min(1) long courseId, @Valid CourseRequest request) throws Exception {
        log.info("Update course id={} title={}", courseId, request.getTitle());
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Owner (nếu truyền)
        if (StringUtils.hasText(request.getOwnerEmail())) {
            User owner = resolveOwner(request.getOwnerEmail());
            course.setOwner(owner);
        }

        // Ảnh (tùy chọn: nếu có file && !empty thì mới thay)
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            UploadResult result = minioChannel.upload(request.getImage());
            course.setImage(result.objectKey()); // cập nhật objectKey mới
        }

        // Các trường khác
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setPrice(request.getPrice());
        course.setDuration(request.getDuration());
        course.setCategory(CourseCategory.fromString(request.getCategory()));

        courseRepository.save(course);
        return mapToCourseResponse(course);
    }

    /* ================== HELPERS ================== */
    private User resolveOwner(String ownerEmail) {
        if (StringUtils.hasText(ownerEmail)) {
            return userRepository.findByEmail(ownerEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Owner not found with email: " + ownerEmail));
        }
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    /** Map entity -> DTO: chuyển imageObjectKey -> presigned GET URL (TTL = default). */
    private CourseResponse mapToCourseResponse(Course course) {
        String presignedUrl = null;
        String key = course.getImage(); // đang lưu objectKey trong DB
        if (StringUtils.hasText(key)) {
            try {
                // TTL = 0 -> MinioChannel tự dùng cấu hình presignExpirySeconds
                presignedUrl = minioChannel.presignedGetUrl(key, 0);
            } catch (Exception e) {
                log.warn("Không tạo được presignedUrl cho key {}: {}", key, e.getMessage());
            }
        }
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                presignedUrl,                    // trả URL tạm cho FE
                course.getCategory().name(),
                course.getPrice(),
                course.getDuration(),
                course.getOwner() != null ? course.getOwner().getEmail() : null
        );
    }

    /** Map request -> entity (chưa set owner & image). */
    public Course mapToCourse(CourseRequest request) {
        return Course.builder()
                .title(request.getTitle().strip())
                .description(request.getDescription().trim())
                .category(CourseCategory.fromString(request.getCategory().strip()))
                .price(request.getPrice())
                .duration(request.getDuration())
                .build();
    }
}
