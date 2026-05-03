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
    private static final long SESSION_EXPIRY_DAYS = 7;

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
                    .expiresAt(resolveNextExpiry())
                    .build());
        }

        UserSession primary = matchingSessions.getFirst();
        UserSession saved = touch(primary, ipAddress, userAgent, deviceId);

        revokeDuplicates(matchingSessions, saved.getId());
        return saved;
    }

    @Transactional
    public UserSession attachSession(Long userId, Long sessionId, String ipAddress, String userAgent, String deviceId) {
        if (sessionId != null) {
            UserSession session = userSessionRepository
                    .findByIdAndUserIdAndRevokedFalseAndExpiresAtAfter(sessionId, userId, LocalDateTime.now())
                    .orElse(null);
            if (session != null) {
                return touch(session, ipAddress, userAgent, deviceId);
            }
        }

        return registerSession(userId, ipAddress, userAgent, deviceId);
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

    @Transactional
    public void revokeSessionExact(Long userId, Long sessionId) {
        if (sessionId == null) {
            return;
        }
        UserSession session = userSessionRepository.findByIdAndUserIdAndRevokedFalse(sessionId, userId)
                .orElseThrow(() -> new AuthenticationException("Session not found"));
        revoke(session);
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
                        .expiresAt(session.getExpiresAt())
                        .build())
                .toList();
    }

    private List<UserSession> findActiveSessions(Long userId) {
        return userSessionRepository.findByUserIdAndRevokedFalseAndExpiresAtAfterOrderByLastSeenAtDescCreatedAtDesc(
                userId,
                LocalDateTime.now());
    }

    private boolean isWithinActiveWindow(UserSession session) {
        return !session.isExpired();
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

    private UserSession touch(UserSession session, String ipAddress, String userAgent, String deviceId) {
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        if (!isBlank(deviceId)) {
            session.setDeviceId(deviceId.trim());
        }
        session.setLastSeenAt(LocalDateTime.now());
        session.setExpiresAt(resolveNextExpiry());
        return userSessionRepository.save(session);
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

    private LocalDateTime resolveNextExpiry() {
        return LocalDateTime.now().plusDays(SESSION_EXPIRY_DAYS);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
