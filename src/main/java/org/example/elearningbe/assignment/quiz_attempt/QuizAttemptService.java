package org.example.elearningbe.assignment.quiz_attempt;

import lombok.RequiredArgsConstructor;
import org.example.elearningbe.assignment.question.Choice;
import org.example.elearningbe.assignment.question.QuestionRepository;
import org.example.elearningbe.assignment.question.dto.ChoiceResponse;
import org.example.elearningbe.assignment.question.dto.QuestionResponse;
import org.example.elearningbe.assignment.quiz.Quiz;
import org.example.elearningbe.assignment.quiz.QuizRepository;
import org.example.elearningbe.assignment.quiz.dto.QuizResponse;
import org.example.elearningbe.assignment.quiz_attempt.dto.response.*;
import org.example.elearningbe.assignment.quiz_attempt.entity.Answer;
import org.example.elearningbe.assignment.quiz_attempt.entity.AnswerExplanation;
import org.example.elearningbe.assignment.quiz_attempt.entity.QuizAttempt;
import org.example.elearningbe.common.AppUtils;
import org.example.elearningbe.common.PageResponse;
import org.example.elearningbe.exception.InvalidDataException;
import org.example.elearningbe.exception.ResourceNotFoundException;
import org.example.elearningbe.integration.ai.AiService;
import org.example.elearningbe.user.entities.User;
import org.example.elearningbe.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AnswerExplanationRepository answerExplanationRepository;
    private final AiService aiService;

    public StartAttemptResponse startAttempt(Long quizId) {
        var quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        String currentEmail = AppUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .user(user)
                .startedAt(LocalDateTime.now())
                .build();
        quizAttemptRepository.save(attempt);

        // Lấy danh sách câu hỏi theo quizId
        List<QuestionResponse> questionResponses = questionRepository.findByQuizId(quizId)
                .stream()
                .map(this::mapToQuestionResponse)
                .toList();

        return new StartAttemptResponse(
                attempt.getId(),
                mapToQuizResponse(quiz),
                user.getId(),
                attempt.getStartedAt(),
                questionResponses
        );
    }

    @Transactional
    public void submitAttempt(Long attemptId, SubmitAttemptRequest request) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt not found"));

        if (attempt.getSubmittedAt() != null) {
            throw new InvalidDataException("This attempt has already been submitted");
        }

        int totalQuestions = request.answers().size();
        int correctCount = 0;

        // Lưu các answer
        for (var ans : request.answers()) {
            var question = questionRepository.findById(ans.questionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
            var choice = question.getChoices().stream()
                    .filter(c -> c.getId().equals(ans.choiceId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Choice not found"));

            // đếm câu đúng
            if (Boolean.TRUE.equals(choice.getIsCorrect())) {
                correctCount++;
            }

            Answer answer = Answer.builder()
                    .attempt(attempt)
                    .question(question)
                    .choice(choice)
                    .build();
            answerRepository.save(answer);
        }

        // Tính điểm = (số câu đúng / tổng số câu) * 100
        int score = totalQuestions > 0 ? (int) ((correctCount * 100.0) / totalQuestions) : 0;
        attempt.setScore(score);

        // cập nhật submittedAt
        attempt.setSubmittedAt(LocalDateTime.now());
        quizAttemptRepository.save(attempt);
    }


    @Transactional(readOnly = true)
    public QuizResultResponse<AnswerResponse> getAttemptResult(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt not found"));

        List<AnswerResponse> answers = attempt.getAnswers().stream()
                .map(ans -> AnswerResponse.builder()
                        .questionId(ans.getQuestion().getId())
                        .choiceId(ans.getChoice().getId())
                        .isCorrect(ans.getChoice().getIsCorrect())
                        .build())
                .toList();

        return new QuizResultResponse<>(
                attempt.getId(),
                mapToQuizResponse(attempt.getQuiz()),
                attempt.getUser().getId(),
                attempt.getScore(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt(),
                answers
        );
    }

    @Transactional(readOnly = true)
    public QuizResultResponse<ReviewAnswerResponse> getAttemptReview(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt not found"));

        List<ReviewAnswerResponse> answers = attempt.getAnswers().stream()
                .map(ans -> {
                    var correctChoice = ans.getQuestion().getChoices().stream()
                            .filter(Choice::getIsCorrect)
                            .findFirst()
                            .orElse(null);
                    return ReviewAnswerResponse.builder()
                            .questionId(ans.getQuestion().getId())
                            .choiceId(ans.getChoice().getId())
                            .isCorrect(ans.getChoice().getIsCorrect())
                            .correctChoiceId(correctChoice != null ? correctChoice.getId() : null)
                            .correctChoiceContent(correctChoice != null ? correctChoice.getContent() : null)
                            .build();
                })
                .toList();

        return new QuizResultResponse<>(
                attempt.getId(),
                mapToQuizResponse(attempt.getQuiz()),
                attempt.getUser().getId(),
                attempt.getScore(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt(),
                answers
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<List<AttemptSummaryResponse>> getMyAttempts(Long quizId, int pageNo, int pageSize) {
        String currentEmail = AppUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<QuizAttempt> page = quizAttemptRepository.findByQuizIdAndUserId(
                quizId,
                user.getId(),
                PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "submittedAt"))
        );

        return PageResponse.<List<AttemptSummaryResponse>>builder()
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalPage(page.getTotalPages())
                .items(page.getContent().stream()
                        .map(attempt -> AttemptSummaryResponse.builder()
                                .attemptId(attempt.getId())
                                .quizId(attempt.getQuiz().getId())
                                .score(attempt.getScore())
                                .startedAt(attempt.getStartedAt())
                                .submittedAt(attempt.getSubmittedAt())
                                .build())
                        .toList())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<List<AttemptAdminResponse>> getAllAttemptsByQuiz(Long quizId, int pageNo, int pageSize) {
        Page<QuizAttempt> page = quizAttemptRepository.findByQuizId(
                quizId,
                PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "submittedAt"))
        );

        return PageResponse.<List<AttemptAdminResponse>>builder()
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalPage(page.getTotalPages())
                .items(page.getContent().stream()
                        .map(attempt -> AttemptAdminResponse.builder()
                                .attemptId(attempt.getId())
                                .quizId(attempt.getQuiz().getId())
                                .userId(attempt.getUser().getId())
                                .userName(attempt.getUser().getFullName())
                                .score(attempt.getScore())
                                .startedAt(attempt.getStartedAt())
                                .submittedAt(attempt.getSubmittedAt())
                                .build())
                        .toList())
                .build();
    }

    @Transactional
    public List<AnswerExplanationResponse> getExplanationsForAttempt(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt not found"));

        return attempt.getAnswers().stream().map(ans -> {
            // check cache
            return answerExplanationRepository.findByAnswerId(ans.getId())
                    .map(exp -> AnswerExplanationResponse.builder()
                            .questionId(ans.getQuestion().getId())
                            .choiceId(ans.getChoice().getId())
                            .isCorrect(ans.getChoice().getIsCorrect())
                            .reasoning(exp.getReasoning())
                            .tip(exp.getTip())
                            .build())
                    .orElseGet(() -> {
                        // gọi Gemini
                        ExplanationDto dto = aiService.explain(
                                ans.getQuestion().getContent(),
                                ans.getChoice().getContent(),
                                ans.getQuestion().getChoices().stream()
                                        .filter(Choice::getIsCorrect)
                                        .map(Choice::getContent)
                                        .findFirst().orElse("N/A")
                        );

                        // lưu cache
                        AnswerExplanation exp = AnswerExplanation.builder()
                                .answer(ans)
                                .reasoning(dto.getReasoning())
                                .tip(dto.getTip())
                                .build();
                        answerExplanationRepository.save(exp);

                        return AnswerExplanationResponse.builder()
                                .questionId(ans.getQuestion().getId())
                                .choiceId(ans.getChoice().getId())
                                .isCorrect(ans.getChoice().getIsCorrect())
                                .reasoning(dto.getReasoning())
                                .tip(dto.getTip())
                                .build();
                    });
        }).toList();
    }

    @Transactional
    public AnswerExplanationResponse getExplanationForAnswer(Long attemptId, Long questionId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt not found"));

        // tìm Answer theo attempt + question
        Answer ans = attempt.getAnswers().stream()
                .filter(a -> a.getQuestion().getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));

        // check cache
        return answerExplanationRepository.findByAnswerId(ans.getId())
                .map(exp -> new AnswerExplanationResponse(
                        ans.getQuestion().getId(),
                        ans.getChoice().getId(),
                        ans.getChoice().getIsCorrect(),
                        exp.getReasoning(),
                        exp.getTip()
                ))
                .orElseGet(() -> {
                    // gọi AI
                    var dto = aiService.explain(
                            ans.getQuestion().getContent(),
                            ans.getChoice().getContent(),
                            ans.getQuestion().getChoices().stream()
                                    .filter(c -> Boolean.TRUE.equals(c.getIsCorrect()))
                                    .map(Choice::getContent)
                                    .findFirst()
                                    .orElse("N/A")
                    );

                    // lưu cache
                    AnswerExplanation exp = AnswerExplanation.builder()
                            .answer(ans)
                            .reasoning(dto.getReasoning())
                            .tip(dto.getTip())
                            .build();
                    answerExplanationRepository.save(exp);

                    return new AnswerExplanationResponse(
                            ans.getQuestion().getId(),
                            ans.getChoice().getId(),
                            ans.getChoice().getIsCorrect(),
                            dto.getReasoning(),
                            dto.getTip()
                    );
                });
    }


    @Transactional
    public void deleteAttempt(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt not found"));

        String currentEmail = AppUtils.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ✅ check quyền
        AppUtils.checkAdminOrCourseOwner(currentUser, attempt.getQuiz().getLesson());

        quizAttemptRepository.delete(attempt);
    }

    // ================= Mapping methods =================

    private QuizResponse mapToQuizResponse(Quiz quiz) {
        return QuizResponse.builder()
                .title(quiz.getTitle())
                .timeLimitSeconds(quiz.getTimeLimitSeconds())
                .passScore(quiz.getPassScore())
                .lessonId(quiz.getLesson().getId())
                .id(quiz.getId())
                .build();
    }

    private QuestionResponse mapToQuestionResponse(org.example.elearningbe.assignment.question.Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .content(question.getContent())
                .type(question.getType())
                .quizId(question.getQuiz().getId())
                .choices(question.getChoices().stream()
                        .map(c -> new ChoiceResponse(c.getId(), c.getContent(), null)) // ẩn isCorrect khi thi
                        .toList())
                .build();
    }
}
