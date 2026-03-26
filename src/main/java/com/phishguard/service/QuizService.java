package com.phishguard.service;

import com.phishguard.dto.QuizResultResponseDTO;
import com.phishguard.dto.QuizSubmissionDTO;
import com.phishguard.entity.QuizQuestion;
import com.phishguard.entity.QuizResult;
import com.phishguard.repository.QuizQuestionRepository;
import com.phishguard.repository.QuizResultRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class QuizService {

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizResultRepository quizResultRepository;

    public QuizService(QuizQuestionRepository quizQuestionRepository,
                       QuizResultRepository quizResultRepository) {
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizResultRepository = quizResultRepository;
    }

    public QuizResultResponseDTO submitQuiz(QuizSubmissionDTO submissionDTO) {

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(submissionDTO.getQuizId());

        int score = 0;
        Map<Long, String> submittedAnswers = submissionDTO.getAnswers();

        for (QuizQuestion question : questions) {
            String correctAnswer = question.getCorrectAnswer();
            String userAnswer = submittedAnswers.get(question.getId());

            if (userAnswer != null && userAnswer.equalsIgnoreCase(correctAnswer)) {
                score++;
            }
        }

        QuizResult result = new QuizResult();
        result.setUserId(submissionDTO.getUserId());
        result.setQuizId(submissionDTO.getQuizId());
        result.setScore(score);
        result.setTotalQuestions(questions.size());
        result.setSubmittedAt(LocalDateTime.now());

        quizResultRepository.save(result);

        return new QuizResultResponseDTO(score, questions.size(), "Quiz submitted successfully");
    }
}