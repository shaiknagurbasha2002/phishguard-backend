package com.phishguard.controller;

import com.phishguard.entity.QuizResult;
import com.phishguard.entity.User;
import com.phishguard.repository.QuizResultRepository;
import com.phishguard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AnalyticsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

    @GetMapping("/analytics")
    public List<Map<String, Object>> getAnalytics() {
        List<User> users = userRepository.findAll();
        List<QuizResult> allResults = quizResultRepository.findAll();

        // Group results by userId
        Map<Long, List<QuizResult>> byUser = new HashMap<>();
        for (QuizResult r : allResults) {
            byUser.computeIfAbsent(r.getUserId(), k -> new ArrayList<>()).add(r);
        }

        List<Map<String, Object>> analytics = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("userId",  user.getId());
            row.put("name",    user.getName());
            row.put("email",   user.getEmail());
            row.put("points",  user.getPoints());

            List<QuizResult> results = byUser.getOrDefault(user.getId(), Collections.emptyList());

            int attempts  = results.size();
            double avgPct = results.stream()
                    .mapToDouble(r -> r.getTotalQuestions() > 0
                            ? (double) r.getScore() / r.getTotalQuestions() * 100 : 0)
                    .average().orElse(0);
            int bestPct   = results.stream()
                    .mapToInt(r -> r.getTotalQuestions() > 0
                            ? (int) Math.round((double) r.getScore() / r.getTotalQuestions() * 100) : 0)
                    .max().orElse(0);
            String lastAt = results.stream()
                    .max(Comparator.comparing(QuizResult::getSubmittedAt))
                    .map(r -> r.getSubmittedAt().toString())
                    .orElse(null);

            row.put("quizAttempts", attempts);
            row.put("avgScore",     (long) Math.round(avgPct));
            row.put("bestScore",    bestPct);
            row.put("lastAttempt",  lastAt);

            analytics.add(row);
        }

        analytics.sort((a, b) ->
                Integer.compare(((Number) b.get("points")).intValue(),
                                ((Number) a.get("points")).intValue()));

        return analytics;
    }
}
