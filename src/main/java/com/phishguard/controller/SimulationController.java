package com.phishguard.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phishguard.config.AdminGuard;
import com.phishguard.model.Simulation;
import com.phishguard.repository.SimulationRepository;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    @Autowired
    private SimulationRepository simulationRepository;

    @Autowired
    private AdminGuard adminGuard;

    @GetMapping
    public List<Simulation> getAllSimulations() {
        return simulationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Simulation> getSimulationById(@PathVariable Long id) {
        return simulationRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createSimulation(@RequestBody Simulation simulation) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        if (simulation.getCreatedAt() == null) {
            simulation.setCreatedAt(java.time.LocalDateTime.now());
        }
        return ResponseEntity.ok(simulationRepository.save(simulation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSimulation(@PathVariable Long id) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        if (!simulationRepository.existsById(id)) return ResponseEntity.notFound().build();
        simulationRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Simulation deleted"));
    }
}
