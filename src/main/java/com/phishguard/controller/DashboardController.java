package com.phishguard.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.phishguard.entity.User;
import com.phishguard.repository.UserRepository;
import com.phishguard.repository.TrainingRepository;
import com.phishguard.repository.QuizRepository;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private QuizRepository quizRepository;

    @GetMapping("/summary")
    public Map<String, Object> getDashboardSummary(
            @RequestParam(value = "userId", required = false) Long userId) {

        Map<String, Object> summary = new HashMap<>();

        List<User> users = userRepository.findAll();
        long totalUsers     = users.size();
        long totalTrainings = trainingRepository.count();
        long totalQuizzes   = quizRepository.count();

        int topScore = users.stream()
                .mapToInt(User::getPoints)
                .max()
                .orElse(0);

        summary.put("totalUsers",     totalUsers);
        summary.put("totalTrainings", totalTrainings);
        summary.put("totalQuizzes",   totalQuizzes);
        summary.put("topScore",       topScore);

        // Calculate rank for the requesting user
        if (userId != null) {
            User requestingUser = users.stream()
                    .filter(u -> u.getId().equals(userId))
                    .findFirst()
                    .orElse(null);

            if (requestingUser != null) {
                // Sort by points descending; rank = position + 1
                List<User> sorted = users.stream()
                        .sorted((a, b) -> Integer.compare(b.getPoints(), a.getPoints()))
                        .toList();

                int rank = 1;
                for (int i = 0; i < sorted.size(); i++) {
                    if (sorted.get(i).getId().equals(userId)) {
                        rank = i + 1;
                        break;
                    }
                }

                summary.put("leaderboard_rank", rank);
                summary.put("full_name",         requestingUser.getName());
                summary.put("email",             requestingUser.getEmail());
                summary.put("total_points",      requestingUser.getPoints());
            }
        }

        return summary;
    }
}