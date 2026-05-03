package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.dto.UserSessionDto;
import com.crm.mcsv_auth.entity.UserSession;
import com.crm.mcsv_auth.exception.AuthenticationException;
import com.crm.mcsv_auth.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSessionManager {

    // Mantener alineado con la vigencia del refresh token.
    private static final long ACTIVE_SESSION_WINDOW_DAYS = 7;

    private final UserSessionRepository userSessionRepository;

    @Transactional
    public UserSession registerSession(Long userId, String ipAddress, String userAgent, String deviceId) {
        List<UserSession> activeSessions = findActiveSessions(userId);
        Set<String> currentKeys = resolveCandidateKeys(deviceId, ipAddress, userAgent);

        List<UserSession> matchingSessions = activeSessions.stream()
                .filter(session -> matches(session, currentKeys))
                .toList();

        if (matchingSessions.isEmpty()) {
            return userSessionRepository.save(UserSession.builder()
                    .userId(userId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .deviceId(normalize(deviceId))
                    .build());
        }

        UserSession primary = matchingSessions.getFirst();
        primary.setIpAddress(ipAddress);
        primary.setUserAgent(userAgent);
        if (isBlank(primary.getDeviceId()) && !isBlank(deviceId)) {
            primary.setDeviceId(deviceId.trim());
        }
        UserSession saved = userSessionRepository.save(primary);

        revokeDuplicates(matchingSessions, saved.getId());
        return saved;
    }

    @Transactional
    public void revokeSessionGroup(Long userId, Long sessionId) {
        UserSession selected = userSessionRepository.findByIdAndUserIdAndRevokedFalse(sessionId, userId)
                .orElseThrow(() -> new AuthenticationException("Session not found"));

        List<UserSession> activeSessions = findActiveSessions(userId);
        Set<String> keys = resolveCandidateKeys(selected.getDeviceId(), selected.getIpAddress(), selected.getUserAgent());

        List<UserSession> matchingSessions = activeSessions.stream()
                .filter(session -> matches(session, keys))
                .toList();

        if (matchingSessions.isEmpty()) {
            revoke(selected);
            return;
        }

        matchingSessions.forEach(this::revoke);
    }

    @Transactional
    public void revokeCurrentSession(Long userId, String ipAddress, String userAgent, String deviceId) {
        List<UserSession> activeSessions = findActiveSessions(userId);
        Set<String> keys = resolveCandidateKeys(deviceId, ipAddress, userAgent);

        List<UserSession> matchingSessions = activeSessions.stream()
                .filter(session -> matches(session, keys))
                .toList();

        if (matchingSessions.isEmpty()) {
            activeSessions.stream().findFirst().ifPresent(this::revoke);
            return;
        }

        matchingSessions.forEach(this::revoke);
    }

    @Transactional(readOnly = true)
    public List<UserSessionDto> listVisibleSessions(Long userId) {
        List<UserSession> visibleSessions = findActiveSessions(userId).stream()
                .filter(this::isWithinActiveWindow)
                .toList();

        Map<String, UserSession> latestByKey = new LinkedHashMap<>();
        for (UserSession session : visibleSessions) {
            latestByKey.putIfAbsent(resolveDisplayKey(session), session);
        }

        return latestByKey.values().stream()
                .map(session -> UserSessionDto.builder()
                        .id(session.getId())
                        .ipAddress(session.getIpAddress())
                        .userAgent(session.getUserAgent())
                        .deviceId(session.getDeviceId())
                        .createdAt(session.getCreatedAt())
                        .lastSeenAt(session.getLastSeenAt())
                        .build())
                .toList();
    }

    private List<UserSession> findActiveSessions(Long userId) {
        return userSessionRepository.findByUserIdAndRevokedFalseOrderByLastSeenAtDescCreatedAtDesc(userId);
    }

    private boolean isWithinActiveWindow(UserSession session) {
        LocalDateTime reference = session.getLastSeenAt() != null ? session.getLastSeenAt() : session.getCreatedAt();
        return reference != null && !reference.isBefore(LocalDateTime.now().minusDays(ACTIVE_SESSION_WINDOW_DAYS));
    }

    private void revokeDuplicates(List<UserSession> sessions, Long keepId) {
        List<UserSession> duplicates = new ArrayList<>();
        for (UserSession session : sessions) {
            if (!session.getId().equals(keepId)) {
                revoke(session);
                duplicates.add(session);
            }
        }

        if (!duplicates.isEmpty()) {
            userSessionRepository.saveAll(duplicates);
            log.info("Revoked {} duplicated session(s) while refreshing active session state", duplicates.size());
        }
    }

    private void revoke(UserSession session) {
        session.setRevoked(true);
        session.setRevokedAt(LocalDateTime.now());
        userSessionRepository.save(session);
    }

    private boolean matches(UserSession session, Set<String> candidateKeys) {
        return candidateKeys.contains(resolveDeviceKey(session.getDeviceId()))
                || candidateKeys.contains(resolveFingerprintKey(session.getIpAddress(), session.getUserAgent()));
    }

    private Set<String> resolveCandidateKeys(String deviceId, String ipAddress, String userAgent) {
        Set<String> keys = new LinkedHashSet<>();
        if (!isBlank(deviceId)) {
            keys.add(resolveDeviceKey(deviceId));
        }
        keys.add(resolveFingerprintKey(ipAddress, userAgent));
        return keys;
    }

    private String resolveDisplayKey(UserSession session) {
        return !isBlank(session.getDeviceId())
                ? resolveDeviceKey(session.getDeviceId())
                : resolveFingerprintKey(session.getIpAddress(), session.getUserAgent());
    }

    private String resolveDeviceKey(String deviceId) {
        return isBlank(deviceId) ? "" : "device:" + deviceId.trim().toLowerCase();
    }

    private String resolveFingerprintKey(String ipAddress, String userAgent) {
        String normalizedIp = normalize(ipAddress);
        String normalizedAgent = normalize(userAgent);
        String raw = normalizedIp + "|" + normalizedAgent;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return "fingerprint:" + Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception e) {
            return "fingerprint:" + raw;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
