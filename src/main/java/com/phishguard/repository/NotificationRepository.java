package com.phishguard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.phishguard.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Fetch notifications for a specific user:
     *   - Notifications where userId matches (targeted)
     *   - OR global notifications (userId IS NULL)
     * Ordered newest first.
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.userId = :userId OR n.userId IS NULL " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findForUser(@Param("userId") Long userId);

    /**
     * Count unread notifications for a user (targeted + global).
     */
    @Query("SELECT COUNT(n) FROM Notification n " +
           "WHERE (n.userId = :userId OR n.userId IS NULL) AND n.isRead = false")
    long countUnreadForUser(@Param("userId") Long userId);
}
