package com.phishguard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.phishguard.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}