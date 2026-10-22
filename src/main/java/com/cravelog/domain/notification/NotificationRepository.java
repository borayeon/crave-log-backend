package com.cravelog.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.recipient.id = :recipientId ORDER BY n.createdAt DESC")
    List<Notification> findAllByRecipientIdOrderByCreatedAtDesc(@Param("recipientId") Long recipientId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isRead = false")
    void markAllAsReadByRecipientId(@Param("recipientId") Long recipientId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient.id = :userId OR n.actor.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
