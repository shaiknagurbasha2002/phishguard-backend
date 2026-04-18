package com.phishguard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.phishguard.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Only UNREAD notifications for this user. */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.userId = :userId AND n.isRead = false " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findForUser(@Param("userId") Long userId);

    /** Only UNREAD notifications for this user (admin uses same query now). */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.userId = :userId AND n.isRead = false " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findTargetedForUser(@Param("userId") Long userId);

    /** Count unread notifications for this user. */
    @Query("SELECT COUNT(n) FROM Notification n " +
           "WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadForUser(@Param("userId") Long userId);

    /** Count unread targeted notifications (same as above — kept for compatibility). */
    @Query("SELECT COUNT(n) FROM Notification n " +
           "WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadTargetedForUser(@Param("userId") Long userId);

    /** Mark only targeted (non-global) notifications as read for a user. */
    @org.springframework.data.jpa.repository.Modifying
    @jakarta.transaction.Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markTargetedAsRead(@Param("userId") Long userId);

    /** Delete only targeted (non-global) read notifications — never touches shared global records. */
    @org.springframework.data.jpa.repository.Modifying
    @jakarta.transaction.Transactional
    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.isRead = true")
    void deleteReadForUser(@Param("userId") Long userId);
}
