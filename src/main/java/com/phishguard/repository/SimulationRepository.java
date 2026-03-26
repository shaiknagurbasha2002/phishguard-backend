package com.phishguard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.phishguard.model.Simulation;

public interface SimulationRepository extends JpaRepository<Simulation, Long> {
}