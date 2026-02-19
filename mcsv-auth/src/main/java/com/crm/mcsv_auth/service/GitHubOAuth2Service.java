package com.crm.mcsv_auth.service;

import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.dto.GitHubUserInfo;

public interface GitHubOAuth2Service {
    String buildAuthorizationUrl();
    AuthResponse authenticate(String code, String ipAddress, String userAgent, String deviceId);
}
