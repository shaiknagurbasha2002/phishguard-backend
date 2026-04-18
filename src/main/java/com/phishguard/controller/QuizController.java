package com.phishguard.controller;

import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phishguard.config.AdminGuard;
import com.phishguard.dto.QuizSubmissionDTO;
import com.phishguard.dto.QuizResultResponseDTO;
import com.phishguard.entity.Quiz;
import com.phishguard.entity.QuizQuestion;
import com.phishguard.entity.QuizResult;
import com.phishguard.repository.QuizRepository;
import com.phishguard.repository.QuizQuestionRepository;
import com.phishguard.repository.QuizResultRepository;
import com.phishguard.service.NotificationService;
import com.phishguard.service.QuizService;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class QuizController {

    @Autowired private QuizRepository quizRepository;
    @Autowired private QuizQuestionRepository quizQuestionRepository;
    @Autowired private QuizResultRepository quizResultRepository;
    @Autowired private QuizService quizService;
    @Autowired private NotificationService notificationService;
    @Autowired private AdminGuard adminGuard;

    // ── Quiz CRUD ─────────────────────────────────────────────────────────────

    @GetMapping("/api/quizzes")
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    @GetMapping("/api/quizzes/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        return quizRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/quizzes")
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        Quiz saved = quizRepository.save(quiz);
        notificationService.notifyAllUsers(
            "New Quiz Available!",
            "New Quiz: " + saved.getTitle(),
            "A new quiz <b>" + saved.getTitle() + "</b> is now available. Login to PhishGuard to test your knowledge!"
        );
        return ResponseEntity.ok(saved);
    }

    @Transactional
    @DeleteMapping("/api/quizzes/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        if (!quizRepository.existsById(id)) return ResponseEntity.notFound().build();
        // Delete questions first to avoid FK violation
        quizQuestionRepository.deleteByQuizId(id);
        quizRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Quiz deleted successfully"));
    }

    // ── Questions for a specific Quiz ─────────────────────────────────────────

    @GetMapping("/api/quizzes/{quizId}/questions")
    public List<QuizQuestion> getQuestionsByQuiz(@PathVariable Long quizId) {
        return quizQuestionRepository.findByQuizId(quizId);
    }

    @PostMapping("/api/quizzes/{quizId}/questions")
    public ResponseEntity<QuizQuestion> addQuestionToQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizQuestion question) {

        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        if (!quizRepository.existsById(quizId)) {
            return ResponseEntity.notFound().build();
        }
        question.setQuizId(quizId);
        QuizQuestion saved = quizQuestionRepository.save(question);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/api/quiz-questions/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        if (!quizQuestionRepository.existsById(id)) return ResponseEntity.notFound().build();
        quizQuestionRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Question deleted"));
    }

    // ── All questions (legacy / quiz interface) ───────────────────────────────

    @GetMapping("/api/quiz-questions")
    public List<QuizQuestion> getAllQuizQuestions() {
        return quizQuestionRepository.findAll();
    }

    @PostMapping("/api/quiz-questions")
    public ResponseEntity<?> createQuizQuestion(@RequestBody QuizQuestion question) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        QuizQuestion saved = quizQuestionRepository.save(question);
        notificationService.notifyAllUsers(
            "New Quiz Available!",
            "New Quiz Question Added",
            "A new quiz question has been added to PhishGuard. Login to test your knowledge!"
        );
        return ResponseEntity.ok(saved);
    }

    // ── Results ───────────────────────────────────────────────────────────────

    @GetMapping("/api/quiz-results/user/{userId}")
    public List<QuizResult> getResultsByUser(@PathVariable Long userId) {
        return quizResultRepository.findByUserId(userId);
    }

    @PostMapping("/api/quiz/submit")
    public ResponseEntity<QuizResultResponseDTO> submitQuiz(@RequestBody QuizSubmissionDTO submission) {
        QuizResultResponseDTO result = quizService.submitQuiz(submission);
        return ResponseEntity.ok(result);
    }
}
