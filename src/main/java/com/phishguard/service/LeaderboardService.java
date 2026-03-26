package com.phishguard.service;

import com.phishguard.dto.LeaderboardDTO;
import com.phishguard.entity.QuizResult;
import com.phishguard.entity.User;
import com.phishguard.repository.QuizResultRepository;
import com.phishguard.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;

    public LeaderboardService(QuizResultRepository quizResultRepository,
                              UserRepository userRepository) {
        this.quizResultRepository = quizResultRepository;
        this.userRepository = userRepository;
    }

    public List<LeaderboardDTO> getLeaderboard() {

        List<QuizResult> results = quizResultRepository.findAll();
        List<User> users = userRepository.findAll();

        Map<Long, String> nameMap  = users.stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        Map<Long, String> emailMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u.getEmail() != null ? u.getEmail() : ""));

        Map<Long, Integer> totalScoreMap = new HashMap<>();

        for (QuizResult result : results) {
            Long userId = result.getUserId();
            int score = result.getScore();
            totalScoreMap.put(userId, totalScoreMap.getOrDefault(userId, 0) + score);
        }

        // Also include users with 0 quiz results so everyone appears on the leaderboard
        for (User user : users) {
            totalScoreMap.putIfAbsent(user.getId(), user.getPoints());
        }

        List<LeaderboardDTO> leaderboard = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : totalScoreMap.entrySet()) {
            leaderboard.add(new LeaderboardDTO(
                    entry.getKey(),
                    nameMap.getOrDefault(entry.getKey(), "Unknown User"),
                    emailMap.getOrDefault(entry.getKey(), ""),
                    entry.getValue()
            ));
        }

        leaderboard.sort((a, b) -> Integer.compare(b.getTotalScore(), a.getTotalScore()));

        return leaderboard;
    }
}