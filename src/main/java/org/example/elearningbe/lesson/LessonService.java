package org.example.elearningbe.lesson;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.example.elearningbe.chapter.ChapterRepository;
import org.example.elearningbe.chapter.entities.Chapter;
import org.example.elearningbe.common.PageResponse;
import org.example.elearningbe.common.enumerate.LessonType;
import org.example.elearningbe.course.CourseRepository;
import org.example.elearningbe.course.entities.Course;
import org.example.elearningbe.integration.minio.MinioChannel;
import org.example.elearningbe.lesson.dto.LessonCreateRequest;
import org.example.elearningbe.lesson.dto.LessonPatchRequest;
import org.example.elearningbe.lesson.dto.LessonResponse;
import org.example.elearningbe.lesson.dto.LessonUpdateRequest;
import org.example.elearningbe.lesson.entities.Lesson;
import org.example.elearningbe.mapper.LessonMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final MinioChannel minioChannel;
    private final LessonMapper lessonMapper;

    @CacheEvict(value = "lessons", allEntries = true)
    public LessonResponse createLesson(LessonCreateRequest req) throws Exception {
        // 1. Lấy course
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // 2. Lấy user hiện tại
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 3. Kiểm tra quyền
        if (!course.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Only course owner can create lessons");
        }

        // 4. Nếu có chapter thì load
        Chapter chapter = null;
        if (req.getChapterId() != null) {
            chapter = chapterRepository.findById(req.getChapterId())
                    .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
        }

        // 5. Tạo entity
        Lesson lesson = Lesson.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .imageObjectKey(req.getImageObjectKey())
                .resourceObjectKey(req.getResourceObjectKey())
                .type(req.getType())
                .course(course)
                .chapter(chapter)
                .durationMinutes(req.getDurationMinutes())
                .orderIndex(req.getOrderIndex())
                .build();

        lessonRepository.save(lesson);

        return lessonMapper.toLessonResponse(lesson);
    }

    public LessonResponse getLesson(Long id) throws Exception {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        return lessonMapper.toLessonResponse(lesson);
    }

    @CacheEvict(value = "lessons", allEntries = true)
    public LessonResponse updateLesson(Long id, LessonUpdateRequest req) throws Exception {
        Lesson lesson = findLessonById(id);

        // --- phần update như trước ---
        lesson.setTitle(req.getTitle());
        lesson.setDescription(req.getDescription());
        lesson.setDurationMinutes(req.getDurationMinutes());
        lesson.setOrderIndex(req.getOrderIndex());

        if (req.getImageObjectKey() != null) {
            lesson.setImageObjectKey(req.getImageObjectKey());
        }
        if (req.getResourceObjectKey() != null) {
            lesson.setResourceObjectKey(req.getResourceObjectKey());
        }
        if (req.getType() != null) lesson.setType(req.getType());

        if (req.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(req.getChapterId())
                    .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
            lesson.setChapter(chapter);
        }

        lessonRepository.save(lesson);

        return lessonMapper.toLessonResponse(lesson);
    }

    @CacheEvict(value = "lessons", allEntries = true)
    public LessonResponse patchLesson(Long id, LessonPatchRequest req) throws Exception {
        Lesson lesson = findLessonById(id);

        // chỉ update khi field != null
        if (req.getTitle() != null) {
            lesson.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            lesson.setDescription(req.getDescription());
        }
        if (req.getImageObjectKey() != null) {
            lesson.setImageObjectKey(req.getImageObjectKey());
        }
        if (req.getResourceObjectKey() != null) {
            lesson.setResourceObjectKey(req.getResourceObjectKey());
        }
        if (req.getType() != null) {
            lesson.setType(req.getType());
        }
        if (req.getDurationMinutes() != null) {
            lesson.setDurationMinutes(req.getDurationMinutes());
        }
        if (req.getOrderIndex() != null) {
            lesson.setOrderIndex(req.getOrderIndex());
        }
        if (req.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(req.getChapterId())
                    .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
            lesson.setChapter(chapter);
        }

        lessonRepository.save(lesson);

        return lessonMapper.toLessonResponse(lesson);
    }

    private Lesson findLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        Course course = lesson.getCourse();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!course.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Only course owner can update lessons");
        }
        return lesson;
    }

    @Cacheable(
            value = "lessons",
            key = "'page=' + #pageNo + ':size=' + #pageSize + ':course=' + #courseId + ':chapter=' + #chapterId + ':type=' + #type + ':title=' + #title"
    )
    public PageResponse<List<LessonResponse>> getLessons(
            int pageNo, int pageSize,
            Long courseId, Long chapterId,
            LessonType type, String title) {

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("orderIndex").ascending());

        Specification<Lesson> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (courseId != null) {
                predicates.add(cb.equal(root.get("course").get("id"), courseId));
            }
            if (chapterId != null) {
                predicates.add(cb.equal(root.get("chapter").get("id"), chapterId));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (title != null && !title.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Lesson> page = lessonRepository.findAll(spec, pageable);

        return getListPageResponse(pageNo, pageSize, page);
    }

    @CacheEvict(value = "lessons", allEntries = true)
    public void deleteLesson(Long id, String currentUsername) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        Course course = lesson.getCourse();
        if (!course.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Only course owner can delete lessons");
        }

        // gom key thành list
        List<String> objectKeys = new ArrayList<>();
        if (lesson.getImageObjectKey() != null) objectKeys.add(lesson.getImageObjectKey());
        if (lesson.getResourceObjectKey() != null) objectKeys.add(lesson.getResourceObjectKey());

        minioChannel.removeObjects(objectKeys);


        lessonRepository.delete(lesson);
    }

    public PageResponse<List<LessonResponse>> getLessonsByCourse(Long courseId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("orderIndex").ascending());
        Page<Lesson> page = lessonRepository.findByCourseId(courseId, pageable);
        return getListPageResponse(pageNo, pageSize, page);
    }

    // ------------------ HELPER METHODS -----------------
    private PageResponse<List<LessonResponse>> getListPageResponse(int pageNo, int pageSize, Page<Lesson> page) {
        List<LessonResponse> responses = page.getContent().stream()
                .map(lesson -> {
                    try {
                        return lessonMapper.toLessonResponse(lesson);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        return PageResponse.<List<LessonResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(responses)
                .build();
    }
}
