package com.phishguard.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phishguard.config.AdminGuard;
import com.phishguard.model.Notification;
import com.phishguard.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AdminGuard adminGuard;

    /**
     * GET /api/notifications/user/{userId}
     * Admin: only targeted notifications (not global user ones).
     * Regular user: targeted + global, newest first.
     */
    @GetMapping("/user/{userId}")
    public List<Notification> getForUser(@PathVariable Long userId) {
        if (adminGuard.isAdmin()) {
            return notificationService.getTargetedForUser(userId);
        }
        return notificationService.getForUser(userId);
    }

    /**
     * GET /api/notifications/user/{userId}/unread-count
     * Admin: count only targeted. Regular user: count targeted + global.
     */
    @GetMapping("/user/{userId}/unread-count")
    public Map<String, Long> getUnreadCount(@PathVariable Long userId) {
        long count = adminGuard.isAdmin()
            ? notificationService.getUnreadCountTargeted(userId)
            : notificationService.getUnreadCount(userId);
        return Map.of("count", count);
    }

    /**
     * PUT /api/notifications/{id}/read
     * Marks a single notification as read.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markRead(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(notificationService.markRead(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PUT /api/notifications/user/{userId}/read-all
     * Marks ALL notifications for a user as read.
     */
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllRead(@PathVariable Long userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/notifications/user/{userId}/read
     * Deletes all read notifications for a user — called after viewing so they disappear.
     */
    @DeleteMapping("/user/{userId}/read")
    public ResponseEntity<Void> deleteRead(@PathVariable Long userId) {
        notificationService.deleteReadForUser(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/notifications
     * Manually create a notification (admin use or testing).
     * Body: { "userId": null|number, "message": "...", "type": "alert", "link": "..." }
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Notification notification) {
        if (!adminGuard.isAdmin()) return adminGuard.forbidden();
        Notification result;
        if (notification.getUserId() == null) {
            result = notificationService.createGlobal(
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getLink()
            );
        } else {
            result = notificationService.createForUser(
                notification.getUserId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getLink()
            );
        }
        return ResponseEntity.ok(result);
    }

}
