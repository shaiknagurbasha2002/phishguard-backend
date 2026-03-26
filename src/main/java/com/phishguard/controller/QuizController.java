package com.phishguard.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phishguard.dto.QuizSubmissionDTO;
import com.phishguard.dto.QuizResultResponseDTO;
import com.phishguard.entity.Quiz;
import com.phishguard.entity.QuizQuestion;
import com.phishguard.entity.QuizResult;
import com.phishguard.repository.QuizRepository;
import com.phishguard.repository.QuizQuestionRepository;
import com.phishguard.repository.QuizResultRepository;
import com.phishguard.service.QuizService;

@RestController
public class QuizController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private QuizService quizService;

    // GET /api/quizzes — list all quizzes
    @GetMapping("/api/quizzes")
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    // POST /api/quizzes — create a quiz
    @PostMapping("/api/quizzes")
    public Quiz createQuiz(@RequestBody Quiz quiz) {
        return quizRepository.save(quiz);
    }

    // DELETE /api/quizzes/{id}
    @DeleteMapping("/api/quizzes/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        if (!quizRepository.existsById(id)) return ResponseEntity.notFound().build();
        quizRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/quiz-questions — return ALL questions (frontend uses this for quiz)
    @GetMapping("/api/quiz-questions")
    public List<QuizQuestion> getAllQuizQuestions() {
        return quizQuestionRepository.findAll();
    }

    // POST /api/quiz-questions — add a new question
    @PostMapping("/api/quiz-questions")
    public QuizQuestion createQuizQuestion(@RequestBody QuizQuestion question) {
        return quizQuestionRepository.save(question);
    }

    // DELETE /api/quiz-questions/{id}
    @DeleteMapping("/api/quiz-questions/{id}")
    public ResponseEntity<Void> deleteQuizQuestion(@PathVariable Long id) {
        if (!quizQuestionRepository.existsById(id)) return ResponseEntity.notFound().build();
        quizQuestionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/quiz-results/user/{userId} — quiz history for a student
    @GetMapping("/api/quiz-results/user/{userId}")
    public List<QuizResult> getResultsByUser(@PathVariable Long userId) {
        return quizResultRepository.findByUserId(userId);
    }

    // POST /api/quiz/submit — submit answers, calculate score, save result
    @PostMapping("/api/quiz/submit")
    public ResponseEntity<QuizResultResponseDTO> submitQuiz(@RequestBody QuizSubmissionDTO submission) {
        QuizResultResponseDTO result = quizService.submitQuiz(submission);
        return ResponseEntity.ok(result);
    }
}