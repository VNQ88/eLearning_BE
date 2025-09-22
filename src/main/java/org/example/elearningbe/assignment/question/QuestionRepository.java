package org.example.elearningbe.assignment.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Page<Question> findByQuizId(Long quizId, Pageable pageable);

    List<Question> findByQuizId(Long quizId);

    @EntityGraph(attributePaths = "choices")
    Optional<Question> findWithChoicesById(Long id);
}

