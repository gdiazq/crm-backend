package com.crm.mcsv_notification.service;

import com.crm.mcsv_notification.dto.NotificationResponse;
import com.crm.mcsv_notification.dto.SendNotificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    void send(SendNotificationRequest request);

    Page<NotificationResponse> list(Long userId, String type, Pageable pageable);

    Map<String, Long> count(Long userId);

    void markAsRead(List<Long> ids, Long userId);

    void markAllAsRead(Long userId);

    void archive(List<Long> ids, Long userId);
}
