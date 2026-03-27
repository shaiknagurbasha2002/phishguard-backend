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

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private AdminGuard adminGuard;

    // GET /api/announcements — public, all users can read
    @GetMapping
    public List<Announcement> getAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    // POST /api/announcements — ADMIN ONLY
    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody Announcement announcement) {

        if (!adminGuard.isAdmin(userIdHeader)) return adminGuard.forbidden();

        announcement.setCreatedAt(LocalDateTime.now());
        Announcement saved = announcementRepository.save(announcement);
        try {
            notificationService.createGlobal(
                "New Announcement",
                saved.getTitle() != null ? saved.getTitle() : saved.getMessage(),
                "announcement",
                "/dashboard"
            );
        } catch (Exception e) {
            System.err.println("Announcement notification failed (non-fatal): " + e.getMessage());
        }
        return ResponseEntity.ok(saved);
    }

    // DELETE /api/announcements/{id} — ADMIN ONLY
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long id) {

        if (!adminGuard.isAdmin(userIdHeader)) return adminGuard.forbidden();

        if (!announcementRepository.existsById(id)) return ResponseEntity.notFound().build();
        announcementRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
