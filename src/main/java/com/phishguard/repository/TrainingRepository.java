package com.phishguard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.phishguard.model.Training;

public interface TrainingRepository extends JpaRepository<Training, Long> {
}
