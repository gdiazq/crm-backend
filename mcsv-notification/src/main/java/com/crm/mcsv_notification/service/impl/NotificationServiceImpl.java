package com.crm.mcsv_notification.service.impl;

import com.crm.mcsv_notification.dto.NotificationResponse;
import com.crm.mcsv_notification.dto.SendNotificationRequest;
import com.crm.mcsv_notification.entity.Notification;
import com.crm.mcsv_notification.exception.NotificationException;
import com.crm.mcsv_notification.repository.NotificationRepository;
import com.crm.mcsv_notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void send(SendNotificationRequest request) {
        log.info("Sending notification to userId: {}", request.getUserId());

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType() != null ? request.getType() : "INFO")
                .build();

        notification = notificationRepository.save(notification);

        NotificationResponse response = toResponse(notification);
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + request.getUserId(),
                response
        );

        log.info("Notification sent and saved with id: {}", notification.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(Long userId, String type, Pageable pageable) {
        log.info("Listing notifications for userId: {}, type: {}", userId, type);

        Page<Notification> page;

        if ("unread".equalsIgnoreCase(type)) {
            page = notificationRepository.findByUserIdAndReadFalseAndArchivedFalseOrderByCreatedAtDesc(userId, pageable);
        } else if ("archived".equalsIgnoreCase(type)) {
            page = notificationRepository.findByUserIdAndArchivedTrueOrderByCreatedAtDesc(userId, pageable);
        } else {
            page = notificationRepository.findByUserIdAndArchivedFalseOrderByCreatedAtDesc(userId, pageable);
        }

        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> count(Long userId) {
        log.info("Counting notifications for userId: {}", userId);

        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("totalInbox", notificationRepository.countByUserIdAndArchivedFalse(userId));
        counts.put("totalUnread", notificationRepository.countByUserIdAndReadFalseAndArchivedFalse(userId));
        counts.put("totalArchived", notificationRepository.countByUserIdAndArchivedTrue(userId));

        return counts;
    }

    @Override
    @Transactional
    public void markAsRead(List<Long> ids, Long userId) {
        log.info("Marking notifications as read: {} for userId: {}", ids, userId);

        List<Notification> notifications = notificationRepository.findAllById(ids);

        for (Notification notification : notifications) {
            if (!notification.getUserId().equals(userId)) {
                throw new NotificationException("Notification " + notification.getId() + " does not belong to user " + userId);
            }
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for userId: {}", userId);
        int updated = notificationRepository.markAllAsReadByUserId(userId);
        log.info("Marked {} notifications as read for userId: {}", updated, userId);
    }

    @Override
    @Transactional
    public void archive(List<Long> ids, Long userId) {
        log.info("Archiving notifications: {} for userId: {}", ids, userId);

        List<Notification> notifications = notificationRepository.findAllById(ids);

        for (Notification notification : notifications) {
            if (!notification.getUserId().equals(userId)) {
                throw new NotificationException("Notification " + notification.getId() + " does not belong to user " + userId);
            }
            notification.setArchived(true);
        }

        notificationRepository.saveAll(notifications);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(notification.getRead())
                .archived(notification.getArchived())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
