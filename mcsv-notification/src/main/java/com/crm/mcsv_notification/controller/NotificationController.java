package com.crm.mcsv_notification.controller;

import com.crm.mcsv_notification.dto.NotificationResponse;
import com.crm.mcsv_notification.dto.SendNotificationRequest;
import com.crm.mcsv_notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for real-time notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @Operation(summary = "Send notification", description = "Send a notification to a user (internal Feign call)")
    public ResponseEntity<Void> send(@Valid @RequestBody SendNotificationRequest request) {
        notificationService.send(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/paged")
    @Operation(summary = "List notifications", description = "List notifications paged (type: null=inbox, unread, archived)")
    public ResponseEntity<Page<NotificationResponse>> list(
            @RequestParam Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> result = notificationService.list(userId, type, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/count")
    @Operation(summary = "Count notifications", description = "Get notification counts for badge (totalInbox, totalUnread, totalArchived)")
    public ResponseEntity<Map<String, Long>> count(@RequestParam Long userId) {
        Map<String, Long> counts = notificationService.count(userId);
        return ResponseEntity.ok(counts);
    }

    @PatchMapping("/read")
    @Operation(summary = "Mark as read", description = "Mark specific notifications as read")
    public ResponseEntity<Void> markAsRead(
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("ids");
        Long userId = ((Number) body.get("userId")).longValue();
        List<Long> ids = rawIds.stream().map(Integer::longValue).toList();
        notificationService.markAsRead(ids, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all non-archived notifications as read")
    public ResponseEntity<Void> markAllAsRead(@RequestBody Map<String, Long> body) {
        notificationService.markAllAsRead(body.get("userId"));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/archive")
    @Operation(summary = "Archive notifications", description = "Archive specific notifications")
    public ResponseEntity<Void> archive(
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("ids");
        Long userId = ((Number) body.get("userId")).longValue();
        List<Long> ids = rawIds.stream().map(Integer::longValue).toList();
        notificationService.archive(ids, userId);
        return ResponseEntity.ok().build();
    }
}
