package com.phishguard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.phishguard.model.Incident;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByUserId(Long userId);
}