package org.example.elearningbe.assignment.quiz_attempt;

import org.example.elearningbe.assignment.quiz_attempt.entity.AnswerExplanation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnswerExplanationRepository extends JpaRepository<AnswerExplanation, Long> {
    Optional<AnswerExplanation> findByAnswerId(Long answerId);
}
