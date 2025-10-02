package org.example.elearningbe.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.common.enumerate.CourseCategory;
import org.example.elearningbe.course.dto.CourseRequest;
import org.example.elearningbe.course.dto.CourseResponse;
import org.example.elearningbe.course.entities.Course;
import org.example.elearningbe.integration.minio.MinioChannel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseMapper {
    private final MinioChannel minioChannel;
    /** Map entity -> DTO: chuyển imageObjectKey -> presigned GET URL (TTL = default). */
    public CourseResponse mapToCourseResponse(Course course) {
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
        assert course.getOwner() != null;
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                presignedUrl,                    // trả URL tạm cho FE
                course.getCategory().name(),
                course.getPrice(),
                course.getDuration(),
                course.getOwner().getEmail(),
                course.getOwner().getAvatar(),
                course.getOwner().getFullName()
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
