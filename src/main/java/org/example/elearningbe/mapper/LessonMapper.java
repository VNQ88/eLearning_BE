package org.example.elearningbe.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.integration.minio.MinioChannel;
import org.example.elearningbe.integration.minio.MinioProps;
import org.example.elearningbe.lesson.dto.LessonResponse;
import org.example.elearningbe.lesson.entities.Lesson;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LessonMapper {
    private final MinioChannel minioChannel;
    private final MinioProps minioProps;

    public LessonResponse toLessonResponse(Lesson lesson) throws Exception {
        String imageUrl = null;
        if (lesson.getImageObjectKey() != null) {
            imageUrl = minioChannel.presignedGetUrl(
                    lesson.getImageObjectKey(),
                    minioProps.getPresignExpirySeconds()
            );
        }

        String resourceUrl = minioChannel.presignedGetUrl(
                lesson.getResourceObjectKey(),
                minioProps.getPresignExpirySeconds()
        );

        return LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .imageUrl(imageUrl)
                .resourceUrl(resourceUrl)
                .type(lesson.getType())
                .orderIndex(lesson.getOrderIndex())
                .courseId(lesson.getCourse().getId())
                .chapterId(lesson.getChapter() != null ? lesson.getChapter().getId() : null)
                .durationMinutes(lesson.getDurationMinutes())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

}
