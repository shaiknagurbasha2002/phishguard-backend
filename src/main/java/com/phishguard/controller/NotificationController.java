package com.phishguard.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phishguard.model.Notification;
import com.phishguard.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * GET /api/notifications/user/{userId}
     * Returns all notifications for a user (targeted + global), newest first.
     */
    @GetMapping("/user/{userId}")
    public List<Notification> getForUser(@PathVariable Long userId) {
        return notificationService.getForUser(userId);
    }

    /**
     * GET /api/notifications/user/{userId}/unread-count
     * Returns just the number of unread notifications (for the bell badge).
     */
    @GetMapping("/user/{userId}/unread-count")
    public Map<String, Long> getUnreadCount(@PathVariable Long userId) {
        return Map.of("count", notificationService.getUnreadCount(userId));
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
     * POST /api/notifications
     * Manually create a notification (admin use or testing).
     * Body: { "userId": null|number, "message": "...", "type": "alert", "link": "..." }
     */
    @PostMapping
    public Notification create(@RequestBody Notification notification) {
        if (notification.getUserId() == null) {
            return notificationService.createGlobal(
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getLink()
            );
        }
        return notificationService.createForUser(
            notification.getUserId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getType(),
            notification.getLink()
        );
    }
}
