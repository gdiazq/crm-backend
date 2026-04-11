package com.crm.lambda.notification;

import java.util.List;

public class SendNotificationRequest {

    private Long userId;
    private List<Long> userIds;
    private String title;
    private String message;
    private String type;

    public SendNotificationRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<Long> resolveUserIds() {
        if (userIds != null && !userIds.isEmpty()) {
            return userIds;
        }
        if (userId != null) {
            return List.of(userId);
        }
        return List.of();
    }
}
