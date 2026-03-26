package com.phishguard.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // null = global notification (visible to all users)
    // non-null = targeted to a specific user
    private Long userId;

    @Column(nullable = false, length = 512)
    private String message;

    // Values: "article" | "training" | "simulation" | "alert"
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Optional: link to the resource (e.g. /dashboard/knowledge/5)
    private String link;

    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.isRead    = false;
    }

    // Convenience constructor used by NotificationService
    public Notification(Long userId, String message, String type, String link) {
        this();
        this.userId  = userId;
        this.message = message;
        this.type    = type;
        this.link    = link;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}
