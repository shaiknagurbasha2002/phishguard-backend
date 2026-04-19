package com.phishguard.controller;

import com.phishguard.config.AdminGuard;
import com.phishguard.model.Announcement;
import com.phishguard.repository.AnnouncementRepository;
import com.phishguard.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private AdminGuard adminGuard;

    @GetMapping
    public List<Announcement> getAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Announcement announcement) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();

        announcement.setCreatedAt(LocalDateTime.now());
        Announcement saved = announcementRepository.save(announcement);
        try {
            notificationService.notifyAllUsers(
                "New Announcement!",
                saved.getTitle() != null ? saved.getTitle() : "Announcement",
                saved.getMessage() != null ? saved.getMessage() : ""
            );
        } catch (Exception e) {
            System.err.println("Notification failed (non-fatal): " + e.getMessage());
        }
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        if (!announcementRepository.existsById(id)) return ResponseEntity.notFound().build();
        announcementRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Announcement deleted"));
    }
}
