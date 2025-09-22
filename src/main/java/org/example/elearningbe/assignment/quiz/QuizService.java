package org.example.elearningbe.assignment.quiz;

import lombok.RequiredArgsConstructor;
import org.example.elearningbe.assignment.quiz.dto.QuizRequest;
import org.example.elearningbe.assignment.quiz.dto.QuizResponse;
import org.example.elearningbe.lesson.LessonRepository;
import org.example.elearningbe.lesson.entities.Lesson;
import org.example.elearningbe.exception.ResourceNotFoundException;
import org.example.elearningbe.common.AppUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final LessonRepository lessonRepository;

    // ================= CRUD =================
    public QuizResponse createQuiz(QuizRequest request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        AppUtils.checkCourseOwner(lesson, AppUtils.getCurrentUserEmail());

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .timeLimitSeconds(request.getTimeLimitSeconds())
                .passScore(request.getPassScore())
                .lesson(lesson)
                .build();

        return toResponse(quizRepository.save(quiz));
    }

    public QuizResponse updateQuiz(Long quizId, QuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        AppUtils.checkCourseOwner(quiz.getLesson(), AppUtils.getCurrentUserEmail());

        quiz.setTitle(request.getTitle());
        quiz.setTimeLimitSeconds(request.getTimeLimitSeconds());
        quiz.setPassScore(request.getPassScore());

        return toResponse(quizRepository.save(quiz));
    }

    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        AppUtils.checkCourseOwner(quiz.getLesson(), AppUtils.getCurrentUserEmail());

        quizRepository.delete(quiz);
    }

    public QuizResponse getQuiz(Long quizId) {
        return quizRepository.findById(quizId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
    }

    public List<QuizResponse> getQuizzesByLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        return lesson.getQuizzes().stream()
                .map(this::toResponse)
                .toList();
    }

    // ================= Mapper =================
    private QuizResponse toResponse(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .timeLimitSeconds(quiz.getTimeLimitSeconds())
                .passScore(quiz.getPassScore())
                .lessonId(quiz.getLesson().getId())
                .build();
    }
}
