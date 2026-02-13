package com.crm.mcsv_notification.scheduler;

import com.crm.mcsv_notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private final NotificationRepository notificationRepository;

    @Value("${notification.cleanup.read-retention-days:30}")
    private int readRetentionDays;

    @Value("${notification.cleanup.archived-retention-days:60}")
    private int archivedRetentionDays;

    @Scheduled(cron = "${notification.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime readCutoff = LocalDateTime.now().minusDays(readRetentionDays);
        LocalDateTime archivedCutoff = LocalDateTime.now().minusDays(archivedRetentionDays);

        int deletedRead = notificationRepository.deleteReadBefore(readCutoff);
        int deletedArchived = notificationRepository.deleteArchivedBefore(archivedCutoff);

        if (deletedRead > 0 || deletedArchived > 0) {
            log.info("Notification cleanup: {} read (>{} days), {} archived (>{} days)",
                    deletedRead, readRetentionDays, deletedArchived, archivedRetentionDays);
        }
    }
}
