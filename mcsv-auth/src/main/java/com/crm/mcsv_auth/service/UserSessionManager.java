package com.crm.mcsv_auth.service;

import com.crm.mcsv_auth.dto.UserSessionDto;
import com.crm.mcsv_auth.entity.UserSession;

import java.util.List;

public interface UserSessionManager {

    UserSession registerSession(Long userId, String ipAddress, String userAgent, String deviceId);

    UserSession attachSession(Long userId, Long sessionId, String ipAddress, String userAgent, String deviceId);

    void revokeCurrentSession(Long userId, String ipAddress, String userAgent, String deviceId);

    void revokeSessionExact(Long userId, Long sessionId);

    List<UserSessionDto> listVisibleSessions(Long userId);
}
