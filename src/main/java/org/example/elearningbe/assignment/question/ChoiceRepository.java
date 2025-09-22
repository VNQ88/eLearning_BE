package org.example.elearningbe.assignment.question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {
    @Transactional
    void deleteByQuestionId(Long questionId);
}

