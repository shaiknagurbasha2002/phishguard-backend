package com.phishguard.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phishguard.model.Incident;
import com.phishguard.repository.IncidentRepository;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    @Autowired
    private IncidentRepository incidentRepository;

    @GetMapping
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<Incident> getIncidentsByUser(@PathVariable Long userId) {
        return incidentRepository.findByUserId(userId);
    }

    @PostMapping
    public Incident createIncident(@RequestBody Incident incident) {
        if (incident.getReportedAt() == null) {
            incident.setReportedAt(java.time.LocalDateTime.now());
        }
        return incidentRepository.save(incident);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Incident> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return incidentRepository.findById(id).map(incident -> {
            String newStatus = body.get("status");
            if (newStatus != null) incident.setStatus(newStatus);
            return ResponseEntity.ok(incidentRepository.save(incident));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        if (!incidentRepository.existsById(id)) return ResponseEntity.notFound().build();
        incidentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
