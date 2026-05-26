package com.crm.mcsv_auth.service;

public interface AuthNotificationService {

    void sendWelcomeNotification(Long userId, String username);

    void sendLoginNotification(Long userId, String username);
}
