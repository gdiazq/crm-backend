package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.client.GitHubApiClient;
import com.crm.mcsv_auth.client.GitHubTokenClient;
import com.crm.mcsv_auth.client.UserClient;
import com.crm.mcsv_auth.config.GitHubOAuth2Config;
import com.crm.mcsv_auth.config.JwtConfig;
import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.dto.CreateUserInternalRequest;
import com.crm.mcsv_auth.dto.GitHubTokenResponse;
import com.crm.mcsv_auth.dto.GitHubUserInfo;
import com.crm.mcsv_auth.dto.UserDTO;
import com.crm.mcsv_auth.entity.RefreshToken;
import com.crm.mcsv_auth.entity.UserSession;
import com.crm.mcsv_auth.exception.AuthenticationException;
import com.crm.mcsv_auth.repository.UserSessionRepository;
import com.crm.mcsv_auth.service.GitHubOAuth2Service;
import com.crm.mcsv_auth.service.TokenService;
import com.crm.mcsv_auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubOAuth2ServiceImpl implements GitHubOAuth2Service {

    private final GitHubTokenClient gitHubTokenClient;
    private final GitHubApiClient gitHubApiClient;
    private final UserClient userClient;
    private final GitHubOAuth2Config gitHubOAuth2Config;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final UserSessionRepository userSessionRepository;
    private final JwtConfig jwtConfig;

    @Override
    public String buildAuthorizationUrl() {
        return UriComponentsBuilder
                .fromHttpUrl("https://github.com/login/oauth/authorize")
                .queryParam("client_id", gitHubOAuth2Config.getClientId())
                .queryParam("redirect_uri", gitHubOAuth2Config.getRedirectUri())
                .queryParam("scope", gitHubOAuth2Config.getScope())
                .build()
                .toUriString();
    }

    @Override
    @Transactional
    public AuthResponse authenticate(String code, String ipAddress, String userAgent, String deviceId) {
        // 1. Intercambiar código por token de GitHub
        GitHubTokenResponse tokenResponse = gitHubTokenClient.exchangeToken(
                gitHubOAuth2Config.getClientId(),
                gitHubOAuth2Config.getClientSecret(),
                code,
                gitHubOAuth2Config.getRedirectUri()
        );

        if (tokenResponse.getError() != null) {
            log.error("GitHub OAuth2 error: {}", tokenResponse.getErrorDescription());
            throw new AuthenticationException("GitHub authentication failed: " + tokenResponse.getErrorDescription());
        }

        // 2. Obtener info del usuario de GitHub
        GitHubUserInfo githubUser = gitHubApiClient.getUserInfo("Bearer " + tokenResponse.getAccessToken());
        log.info("GitHub user authenticated: {}", githubUser.getLogin());

        if (githubUser.getEmail() == null || githubUser.getEmail().isBlank()) {
            throw new AuthenticationException("GitHub account does not have a public email. Please add a public email in your GitHub profile.");
        }

        // 3. Buscar o crear usuario
        UserDTO user = findOrCreateUser(githubUser);

        // 4. Actualizar last login
        userClient.updateLastLogin(user.getId());

        // 5. Generar tokens
        Set<String> roles = user.getRoles().stream()
                .map(UserDTO.RoleDTO::getName)
                .collect(Collectors.toSet());

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), roles);
        RefreshToken refreshToken = tokenService.createRefreshToken(user.getId());

        // 6. Revocar sesión anterior del mismo dispositivo si aplica
        if (deviceId != null && !deviceId.isBlank()) {
            userSessionRepository.findByUserIdAndDeviceIdAndRevokedFalse(user.getId(), deviceId)
                    .ifPresent(oldSession -> {
                        oldSession.setRevoked(true);
                        oldSession.setRevokedAt(LocalDateTime.now());
                        userSessionRepository.save(oldSession);
                    });
        }

        userSessionRepository.save(UserSession.builder()
                .userId(user.getId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceId(deviceId)
                .build());

        String avatarUrl = user.getProfile() != null ? user.getProfile().getAvatarUrl() : null;

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getPlainToken())
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpireAt() * 60)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .avatarUrl(avatarUrl)
                        .roles(roles)
                        .build())
                .build();
    }

    private UserDTO findOrCreateUser(GitHubUserInfo githubUser) {
        // Buscar por email
        try {
            ResponseEntity<UserDTO> response = userClient.getUserByEmail(githubUser.getEmail());
            if (response.getBody() != null) {
                log.info("Existing user found for GitHub login: {}", githubUser.getEmail());
                return response.getBody();
            }
        } catch (Exception e) {
            log.debug("User not found by email, creating new user for GitHub login");
        }

        // Crear nuevo usuario
        String[] nameParts = splitName(githubUser.getName(), githubUser.getLogin());
        String username = resolveUsername(githubUser.getLogin());

        CreateUserInternalRequest createRequest = CreateUserInternalRequest.builder()
                .username(username)
                .email(githubUser.getEmail())
                .password("Gh@" + UUID.randomUUID().toString().toUpperCase().replace("-", "1")) // password aleatorio, no lo usará
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .roleIds(Set.of(1L)) // ROLE_USER por defecto
                .build();

        ResponseEntity<UserDTO> response = userClient.signUpUser(createRequest);
        if (response.getBody() == null) {
            throw new AuthenticationException("Failed to create user from GitHub account");
        }

        // Marcar email como verificado (GitHub ya lo verificó)
        userClient.verifyEmail(response.getBody().getId());

        log.info("New user created from GitHub login: {}", username);
        return response.getBody();
    }

    private String resolveUsername(String githubLogin) {
        // Intentar usar el login de GitHub, si ya existe agregar sufijo
        try {
            userClient.getUserByUsername(githubLogin);
            return githubLogin + "_gh" + UUID.randomUUID().toString().substring(0, 4);
        } catch (Exception e) {
            return githubLogin;
        }
    }

    private String[] splitName(String fullName, String fallback) {
        if (fullName == null || fullName.isBlank()) {
            return new String[]{fallback, ""};
        }
        int space = fullName.indexOf(' ');
        if (space == -1) {
            return new String[]{fullName, ""};
        }
        return new String[]{fullName.substring(0, space), fullName.substring(space + 1)};
    }
}
