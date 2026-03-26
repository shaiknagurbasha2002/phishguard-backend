package com.phishguard.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phishguard.model.Simulation;
import com.phishguard.repository.SimulationRepository;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    @Autowired
    private SimulationRepository simulationRepository;

    @GetMapping
    public List<Simulation> getAllSimulations() {
        return simulationRepository.findAll();
    }

    @GetMapping("/{id}")
    public Simulation getSimulationById(@PathVariable Long id) {
        return simulationRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Simulation createSimulation(@RequestBody Simulation simulation) {
        if (simulation.getCreatedAt() == null) {
            simulation.setCreatedAt(java.time.LocalDateTime.now());
        }
        return simulationRepository.save(simulation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSimulation(@PathVariable Long id) {
        if (!simulationRepository.existsById(id)) return ResponseEntity.notFound().build();
        simulationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
