package com.phishguard.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.phishguard.model.Notification;
import com.phishguard.repository.NotificationRepository;
import com.phishguard.repository.UserRepository;
import com.phishguard.entity.User;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public void notifyAllUsers(String subject, String title, String message) {
        try {
            List<User> users = userRepository.findAll();
            for (User user : users) {
                try {
                    String role = user.getRole();
                    if (role != null && role.toUpperCase().contains("ADMIN")) continue;
                    createForUser(user.getId(), title, message, "INFO", "/dashboard/training");
                    if (Boolean.TRUE.equals(user.isEmailVerified())) {
                        emailService.sendNotificationEmail(user.getEmail(), subject, title, message);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to notify user " + user.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("notifyAllUsers failed: " + e.getMessage());
        }
    }

    // ── Create helpers ───────────────────────────────────────────────────────

    /** Notify all admin users — used for incident reports. */
    public void notifyAdmins(String title, String message, String type, String link) {
        try {
            userRepository.findByRole("ROLE_ADMIN").forEach(admin -> {
                try {
                    createForUser(admin.getId(), title, message, type, link);
                } catch (Exception e) {
                    System.err.println("Failed to notify admin " + admin.getId() + ": " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("notifyAdmins failed: " + e.getMessage());
        }
    }

    /** @deprecated Use notifyAdmins or notifyAllUsers instead. Kept for compatibility. */
    public Notification createGlobal(String title, String message, String type, String link) {
        Notification n = new Notification(null, title, message, type, link);
        return notificationRepository.save(n);
    }

    /**
     * Create a notification targeted to one specific user.
     */
    public Notification createForUser(Long userId, String title, String message, String type, String link) {
        Notification n = new Notification(userId, title, message, type, link);
        return notificationRepository.save(n);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /** All notifications for a user (targeted + global), newest first. */
    public List<Notification> getForUser(Long userId) {
        return notificationRepository.findForUser(userId);
    }

    /** Only targeted notifications — for admin accounts. */
    public List<Notification> getTargetedForUser(Long userId) {
        return notificationRepository.findTargetedForUser(userId);
    }

    /** Delete all read notifications for a user. */
    public void deleteReadForUser(Long userId) {
        notificationRepository.deleteReadForUser(userId);
    }

    /** Unread count for the bell badge (targeted + global). */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadForUser(userId);
    }

    /** Unread count for admin bell — only targeted, no global user notifications. */
    public long getUnreadCountTargeted(Long userId) {
        return notificationRepository.countUnreadTargetedForUser(userId);
    }

    // ── Mark as read ─────────────────────────────────────────────────────────

    /** Mark a single notification as read. */
    public Notification markRead(Long notificationId) {
        return notificationRepository.findById(notificationId).map(n -> {
            n.setRead(true);
            return notificationRepository.save(n);
        }).orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
    }

    /** Mark only this user's targeted notifications as read — never touches shared global records. */
    public void markAllRead(Long userId) {
        notificationRepository.markTargetedAsRead(userId);
    }
}
