// src/main/java/org/example/elearningbe/chapter/ChapterService.java
package org.example.elearningbe.chapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.chapter.dto.*;
import org.example.elearningbe.chapter.entities.Chapter;
import org.example.elearningbe.course.entities.Course;
import org.example.elearningbe.course.CourseRepository;
import org.example.elearningbe.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public ChapterResponse create(ChapterCreateRequest req) {
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + req.getCourseId()));

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (course.getOwner() == null || !course.getOwner().getEmail().equals(currentEmail)) {
            throw new AccessDeniedException("Bạn không phải tác giả của khóa học này");
        }

        Chapter ch = new Chapter();
        ch.setCourse(course);
        ch.setTitle(req.getTitle());

        // Nếu không truyền orderIndex → gán next = count hiện tại (0-based, dồn khít)
        Integer idx = (req.getOrderIndex() != null) ? req.getOrderIndex() : nextIndex(course.getId());

        // Tránh trùng index trong cùng course
        if (chapterRepository.existsByCourseIdAndOrderIndex(course.getId(), idx)) {
            throw new IllegalArgumentException("orderIndex đã tồn tại trong course " + course.getId());
        }
        ch.setOrderIndex(idx);

        chapterRepository.save(ch);
        return toDto(ch);
    }

    @Transactional(readOnly = true)
    public List<ChapterResponse> listByCourse(Long courseId) {
        ensureCourse(courseId);
        return chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ChapterResponse get(Long chapterId) {
        Chapter ch = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found: " + chapterId));
        return toDto(ch);
    }

    @Transactional
    public ChapterResponse update(Long chapterId, ChapterUpdateRequest req) {
        Chapter ch = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found: " + chapterId));

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (ch.getCourse().getOwner() == null || !ch.getCourse().getOwner().getEmail().equals(currentEmail)) {
            throw new AccessDeniedException("Bạn không phải tác giả của khóa học này");
        }
        ch.setTitle(req.getTitle());
        if (req.getOrderIndex() != null) {
            Integer idx = req.getOrderIndex();
            // nếu đổi index → kiểm tra trùng trong cùng course
            if (!idx.equals(ch.getOrderIndex()) &&
                    chapterRepository.existsByCourseIdAndOrderIndex(ch.getCourse().getId(), idx)) {
                throw new IllegalArgumentException("orderIndex đã tồn tại trong course " + ch.getCourse().getId());
            }
            ch.setOrderIndex(req.getOrderIndex());
        }
        chapterRepository.save(ch);
        return toDto(ch);
    }

    @Transactional
    public void delete(Long chapterId) {
        Chapter ch = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found: " + chapterId));
        chapterRepository.delete(ch);
    }

    @Transactional
    public ChapterResponse reorder(Long chapterId, Integer newOrderIndex) {
        Chapter ch = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found: " + chapterId));
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (ch.getCourse().getOwner() == null || !ch.getCourse().getOwner().getEmail().equals(currentEmail)) {
            throw new AccessDeniedException("Bạn không phải tác giả của khóa học này");
        }
        if (!newOrderIndex.equals(ch.getOrderIndex()) &&
                chapterRepository.existsByCourseIdAndOrderIndex(ch.getCourse().getId(), newOrderIndex)) {
            throw new IllegalArgumentException("orderIndex đã tồn tại trong course " + ch.getCourse().getId());
        }
        ch.setOrderIndex(newOrderIndex);
        chapterRepository.save(ch);
        return toDto(ch);
    }

    @Transactional
    public List<ChapterResponse> reindexBulk(Long courseId, List<ChapterReindexRequest> requests) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (course.getOwner() == null || !course.getOwner().getEmail().equals(currentEmail)) {
            throw new AccessDeniedException("Bạn không phải tác giả của khóa học này");
        }

        // 1. Check duplicate orderIndex trong request
        Set<Integer> idxSet = new HashSet<>();
        for (ChapterReindexRequest req : requests) {
            if (!idxSet.add(req.getOrderIndex())) {
                throw new IllegalArgumentException(
                        "Duplicate orderIndex trong request: " + req.getOrderIndex()
                );
            }
        }

        // 2. Load tất cả chapter liên quan (1 lần)
        List<Long> ids = requests.stream().map(ChapterReindexRequest::getChapterId).toList();
        List<Chapter> chapters = chapterRepository.findAllById(ids);

        if (chapters.size() != requests.size()) {
            throw new ResourceNotFoundException("Một số chapterId không tồn tại trong DB");
        }

        // 3. Map chapterId -> orderIndex mới
        Map<Long, Integer> newIndexMap = requests.stream()
                .collect(Collectors.toMap(ChapterReindexRequest::getChapterId, ChapterReindexRequest::getOrderIndex));

        // 4. Update index (check cùng course)
        for (Chapter ch : chapters) {
            if (!ch.getCourse().getId().equals(courseId)) {
                throw new IllegalArgumentException(
                        "Chapter " + ch.getId() + " không thuộc course " + courseId
                );
            }
            ch.setOrderIndex(newIndexMap.get(ch.getId()));
        }

        // 5. Save all trong 1 batch
        chapterRepository.saveAll(chapters);

        // 6. Trả về list DTO đã update (FE render lại ngay)
        return chapters.stream().map(this::toDto).toList();
    }


    /* Helpers */
    private ChapterResponse toDto(Chapter ch) {
        return new ChapterResponse(
                ch.getId(),
                ch.getCourse().getId(),
                ch.getTitle(),
                ch.getOrderIndex(),
                ch.getCreatedAt(),
                ch.getUpdatedAt()
        );
    }

    private void ensureCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }
    }

    private Integer nextIndex(Long courseId) {
        Integer count = chapterRepository.countByCourseId(courseId);
        return count == null ? 0 : count;
    }
}
