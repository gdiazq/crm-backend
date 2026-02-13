package com.crm.mcsv_notification.repository;

import com.crm.mcsv_notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdAndArchivedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndReadFalseAndArchivedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndArchivedTrueOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndArchivedFalse(Long userId);

    long countByUserIdAndReadFalseAndArchivedFalse(Long userId);

    long countByUserIdAndArchivedTrue(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.archived = false AND n.read = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.read = true AND n.readAt < :cutoff")
    int deleteReadBefore(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.archived = true AND n.createdAt < :cutoff")
    int deleteArchivedBefore(@Param("cutoff") LocalDateTime cutoff);
}
