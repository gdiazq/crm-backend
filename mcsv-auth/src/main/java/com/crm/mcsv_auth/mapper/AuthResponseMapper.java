package com.crm.mcsv_auth.mapper;

import com.crm.mcsv_auth.config.JwtConfig;
import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Arma los DTOs de respuesta de autenticación ({@link AuthResponse} y {@link AuthResponse.UserInfo})
 * a partir del {@link UserDTO}. Concentra aquí el aplanado de roles/permisos.
 */
@Component
@RequiredArgsConstructor
public class AuthResponseMapper {

    private final JwtConfig jwtConfig;

    public AuthResponse toAuthResponse(String accessToken, String refreshToken, UserDTO user) {
        return toAuthResponse(accessToken, refreshToken, user, extractRoles(user), user.getAvatarUrl());
    }

    public AuthResponse toAuthResponse(String accessToken, String refreshToken, UserDTO user,
                                       Set<String> roles, String avatarUrl) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpireAt() * 60)
                .user(toUserInfo(user, roles, avatarUrl))
                .build();
    }

    public AuthResponse.UserInfo toUserInfo(UserDTO user, Set<String> roles, String avatarUrl) {
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

    public AuthResponse.UserInfo toCurrentUserInfo(UserDTO user) {
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

    public Set<String> extractRoles(UserDTO user) {
        return user.getRoles().stream()
                .map(UserDTO.RoleDTO::getName)
                .collect(Collectors.toSet());
    }

    public Set<String> extractPermissions(UserDTO user) {
        return user.getRoles().stream()
                .filter(r -> r.getPermissions() != null)
                .flatMap(r -> r.getPermissions().stream())
                .map(UserDTO.PermissionDTO::getName)
                .collect(Collectors.toSet());
    }
}
