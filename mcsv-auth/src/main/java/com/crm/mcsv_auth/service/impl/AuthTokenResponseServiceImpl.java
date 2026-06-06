package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.dto.UserDTO;
import com.crm.mcsv_auth.entity.RefreshToken;
import com.crm.mcsv_auth.entity.UserSession;
import com.crm.mcsv_auth.mapper.AuthResponseMapper;
import com.crm.mcsv_auth.service.AuthTokenResponseService;
import com.crm.mcsv_auth.service.TokenService;
import com.crm.mcsv_auth.service.UserSessionManager;
import com.crm.mcsv_auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthTokenResponseServiceImpl implements AuthTokenResponseService {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final UserSessionManager userSessionManager;
    private final AuthResponseMapper authResponseMapper;

    @Override
    public AuthResponse createSessionResponse(UserDTO user, String ipAddress, String userAgent, String deviceId) {
        return createSessionResponse(user, ipAddress, userAgent, deviceId, user.getAvatarUrl());
    }

    @Override
    public AuthResponse createSessionResponse(UserDTO user, String ipAddress, String userAgent, String deviceId, String avatarUrl) {
        Set<String> roles = authResponseMapper.extractRoles(user);
        Set<String> permissions = authResponseMapper.extractPermissions(user);
        UserSession session = userSessionManager.registerSession(user.getId(), ipAddress, userAgent, deviceId);
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), roles, permissions, session.getId());
        RefreshToken refreshToken = tokenService.createRefreshToken(user.getId(), session.getId());

        return authResponseMapper.toAuthResponse(accessToken, refreshToken.getPlainToken(), user, roles, avatarUrl);
    }

    @Override
    public String createAccessToken(UserDTO user, Long sessionId) {
        return jwtUtil.generateAccessToken(user.getId(), user.getUsername(),
                authResponseMapper.extractRoles(user), authResponseMapper.extractPermissions(user), sessionId);
    }
}
