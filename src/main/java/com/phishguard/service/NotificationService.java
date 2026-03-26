package com.phishguard.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.phishguard.model.Notification;
import com.phishguard.repository.NotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // ── Create helpers ───────────────────────────────────────────────────────

    /**
     * Create a GLOBAL notification (visible to all users).
     * Used when admin publishes an article or adds a training module.
     */
    public Notification createGlobal(String message, String type, String link) {
        Notification n = new Notification(null, message, type, link);
        return notificationRepository.save(n);
    }

    /**
     * Create a notification targeted to one specific user.
     */
    public Notification createForUser(Long userId, String message, String type, String link) {
        Notification n = new Notification(userId, message, type, link);
        return notificationRepository.save(n);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /** All notifications for a user (targeted + global), newest first. */
    public List<Notification> getForUser(Long userId) {
        return notificationRepository.findForUser(userId);
    }

    /** Unread count for the bell badge. */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadForUser(userId);
    }

    // ── Mark as read ─────────────────────────────────────────────────────────

    /** Mark a single notification as read. */
    public Notification markRead(Long notificationId) {
        return notificationRepository.findById(notificationId).map(n -> {
            n.setRead(true);
            return notificationRepository.save(n);
        }).orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
    }

    /** Mark ALL notifications for a user as read (bulk "mark all read"). */
    public void markAllRead(Long userId) {
        List<Notification> list = notificationRepository.findForUser(userId);
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }
}
