package org.example.elearningbe.chapter;

import org.example.elearningbe.chapter.entities.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    boolean existsByCourseIdAndOrderIndex(Long courseId, Integer index);
    Integer countByCourseId(Long courseId);
}
