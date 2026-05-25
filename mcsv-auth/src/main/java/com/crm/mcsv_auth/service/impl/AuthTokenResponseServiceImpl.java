package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.config.JwtConfig;
import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.dto.UserDTO;
import com.crm.mcsv_auth.entity.RefreshToken;
import com.crm.mcsv_auth.entity.UserSession;
import com.crm.mcsv_auth.service.AuthTokenResponseService;
import com.crm.mcsv_auth.service.TokenService;
import com.crm.mcsv_auth.service.UserSessionManager;
import com.crm.mcsv_auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthTokenResponseServiceImpl implements AuthTokenResponseService {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final UserSessionManager userSessionManager;
    private final JwtConfig jwtConfig;

    @Override
    public AuthResponse createSessionResponse(UserDTO user, String ipAddress, String userAgent, String deviceId) {
        return createSessionResponse(user, ipAddress, userAgent, deviceId, user.getAvatarUrl());
    }

    @Override
    public AuthResponse createSessionResponse(UserDTO user, String ipAddress, String userAgent, String deviceId, String avatarUrl) {
        Set<String> roles = extractRoles(user);
        Set<String> permissions = extractPermissions(user);
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), roles, permissions);
        UserSession session = userSessionManager.registerSession(user.getId(), ipAddress, userAgent, deviceId);
        RefreshToken refreshToken = tokenService.createRefreshToken(user.getId(), session.getId());

        return buildAuthResponse(accessToken, refreshToken.getPlainToken(), user, roles, avatarUrl);
    }

    @Override
    public AuthResponse buildAuthResponse(String accessToken, String refreshToken, UserDTO user) {
        return buildAuthResponse(accessToken, refreshToken, user, extractRoles(user), user.getAvatarUrl());
    }

    @Override
    public String createAccessToken(UserDTO user) {
        return jwtUtil.generateAccessToken(user.getId(), user.getUsername(), extractRoles(user), extractPermissions(user));
    }

    @Override
    public AuthResponse.UserInfo buildCurrentUserInfo(UserDTO user) {
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .roles(extractRoles(user))
                .phoneNumber(user.getPhoneNumber())
                .permissions(extractPermissions(user))
                .build();
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, UserDTO user, Set<String> roles, String avatarUrl) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpireAt() * 60)
                .user(buildUserInfo(user, roles, avatarUrl))
                .build();
    }

    private AuthResponse.UserInfo buildUserInfo(UserDTO user, Set<String> roles, String avatarUrl) {
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(avatarUrl)
                .roles(roles)
                .build();
    }

    private Set<String> extractRoles(UserDTO user) {
        return user.getRoles().stream()
                .map(UserDTO.RoleDTO::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> extractPermissions(UserDTO user) {
        return user.getRoles().stream()
                .filter(r -> r.getPermissions() != null)
                .flatMap(r -> r.getPermissions().stream())
                .map(UserDTO.PermissionDTO::getName)
                .collect(Collectors.toSet());
    }
}
