package com.phishguard.controller;

import com.phishguard.config.AdminGuard;
import com.phishguard.repository.UserRepository;
import com.phishguard.repository.TrainingRepository;
import com.phishguard.repository.QuizRepository;
import com.phishguard.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private TrainingRepository trainingRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private IncidentRepository incidentRepository;
    @Autowired private AdminGuard adminGuard;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        return ResponseEntity.ok(Map.of(
            "totalUsers",     userRepository.count(),
            "totalTrainings", trainingRepository.count(),
            "totalQuizzes",   quizRepository.count(),
            "totalIncidents", incidentRepository.count()
        ));
    }
}