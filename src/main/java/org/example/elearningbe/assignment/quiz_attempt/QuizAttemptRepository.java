package org.example.elearningbe.assignment.quiz_attempt;

import org.example.elearningbe.assignment.quiz_attempt.entity.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Page<QuizAttempt> findByQuizIdAndUserIdAndSubmittedAtIsNotNull(Long quizId, Long userId, Pageable pageable);

    Page<QuizAttempt> findByQuizId(Long quizId, Pageable pageable);

}
