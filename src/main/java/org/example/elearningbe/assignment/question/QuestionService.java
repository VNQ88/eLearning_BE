package org.example.elearningbe.assignment.question;

import lombok.RequiredArgsConstructor;
import org.example.elearningbe.assignment.question.dto.ChoiceResponse;
import org.example.elearningbe.assignment.question.dto.QuestionRequest;
import org.example.elearningbe.assignment.question.dto.QuestionResponse;
import org.example.elearningbe.assignment.quiz.Quiz;
import org.example.elearningbe.assignment.quiz.QuizRepository;
import org.example.elearningbe.common.PageResponse;
import org.example.elearningbe.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final QuizRepository quizRepository;

    // ================= CREATE =================
    @Transactional
    public QuestionResponse createQuestion(QuestionRequest request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        Question question = Question.builder()
                .content(request.getContent())
                .type(request.getType())
                .quiz(quiz)
                .build();

        questionRepository.save(question);

        List<Choice> choices = request.getChoices().stream()
                .map(c -> Choice.builder()
                        .content(c.getContent())
                        .isCorrect(c.getIsCorrect())
                        .question(question)
                        .build())
                .map(choiceRepository::save)
                .toList();

        return toResponse(question, toChoiceResponses(choices));
    }

    // ================= UPDATE =================
    @Transactional
    public QuestionResponse updateQuestion(Long questionId, QuestionRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        question.setContent(request.getContent());
        question.setType(request.getType());
        questionRepository.save(question);

        // xoá toàn bộ choice cũ
        choiceRepository.deleteByQuestionId(questionId);

        // thêm lại choices mới
        List<Choice> newChoices = request.getChoices().stream()
                .map(c -> Choice.builder()
                        .content(c.getContent())
                        .isCorrect(c.getIsCorrect())
                        .question(question)
                        .build())
                .map(choiceRepository::save)
                .toList();

        return toResponse(question, toChoiceResponses(newChoices));
    }

    // ================= GET ONE =================
    @Transactional(readOnly = true)
    public QuestionResponse getQuestion(Long questionId) {
        Question question = questionRepository.findWithChoicesById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        return toResponse(question, toChoiceResponses(question.getChoices()));
    }

    @Transactional(readOnly = true)
    public PageResponse<?> getQuestionsByQuiz(Long quizId, int pageNo, int pageSize) {
        Page<Question> page = questionRepository.findByQuizId(quizId, PageRequest.of(pageNo, pageSize));

        return PageResponse.builder()
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalPage(page.getTotalPages())
                .items(page.getContent().stream()
                        .map(q -> toResponse(q, toChoiceResponses(q.getChoices())))
                        .toList())
                .build();
    }

    // ================= DELETE =================
    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        choiceRepository.deleteAll(question.getChoices());
        questionRepository.delete(question);
    }

    // ================= MAPPING =================
    private QuestionResponse toResponse(Question q, List<ChoiceResponse> choices) {
        return QuestionResponse.builder()
                .id(q.getId())
                .content(q.getContent())
                .type(q.getType())
                .quizId(q.getQuiz().getId())
                .choices(choices)
                .build();
    }

    private List<ChoiceResponse> toChoiceResponses(List<Choice> choices) {
        return choices.stream()
                .map(c -> new ChoiceResponse(c.getId(), c.getContent(), c.getIsCorrect()))
                .toList();
    }
}
