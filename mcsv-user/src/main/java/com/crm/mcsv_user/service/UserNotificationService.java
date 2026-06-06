package com.crm.mcsv_user.service;

public interface UserNotificationService {

    void sendWelcomeNotification(Long userId, String username);

    void sendProfileUpdatedNotification(Long userId);
}
