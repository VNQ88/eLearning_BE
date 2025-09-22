package org.example.elearningbe.assignment.quiz_attempt;

import org.example.elearningbe.assignment.quiz_attempt.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
}
