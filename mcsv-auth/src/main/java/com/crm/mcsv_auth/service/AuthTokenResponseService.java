package com.crm.mcsv_auth.service;

import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.dto.UserDTO;

public interface AuthTokenResponseService {

    AuthResponse createSessionResponse(UserDTO user, String ipAddress, String userAgent, String deviceId);

    AuthResponse createSessionResponse(UserDTO user, String ipAddress, String userAgent, String deviceId, String avatarUrl);

    AuthResponse buildAuthResponse(String accessToken, String refreshToken, UserDTO user);

    String createAccessToken(UserDTO user);

    AuthResponse.UserInfo buildCurrentUserInfo(UserDTO user);
}
