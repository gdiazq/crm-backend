package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.dto.UserSessionDto;
import com.crm.mcsv_auth.entity.UserSession;
import com.crm.mcsv_auth.exception.AuthenticationException;
import com.crm.mcsv_auth.repository.UserSessionRepository;
import com.crm.mcsv_auth.service.UserSessionManager;
import com.crm.mcsv_auth.util.SessionFingerprint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionManagerImpl implements UserSessionManager {

    // Mantener alineado con la vigencia del refresh token.
    private static final long SESSION_EXPIRY_DAYS = 7;

    private final UserSessionRepository userSessionRepository;
    private final SessionFingerprint sessionFingerprint;

    @Override
    @Transactional
    public UserSession registerSession(Long userId, String ipAddress, String userAgent, String deviceId) {
        List<UserSession> activeSessions = findActiveSessions(userId);
        Set<String> currentKeys = sessionFingerprint.candidateKeys(deviceId, ipAddress, userAgent);

        List<UserSession> matchingSessions = activeSessions.stream()
                .filter(session -> sessionFingerprint.matches(session, currentKeys))
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

    @Override
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

    @Override
    @Transactional
    public void revokeCurrentSession(Long userId, String ipAddress, String userAgent, String deviceId) {
        List<UserSession> activeSessions = findActiveSessions(userId);
        Set<String> keys = sessionFingerprint.candidateKeys(deviceId, ipAddress, userAgent);

        List<UserSession> matchingSessions = activeSessions.stream()
                .filter(session -> sessionFingerprint.matches(session, keys))
                .toList();

        if (matchingSessions.isEmpty()) {
            activeSessions.stream().findFirst().ifPresent(this::revoke);
            return;
        }

        matchingSessions.forEach(this::revoke);
    }

    @Override
    @Transactional
    public void revokeSessionExact(Long userId, Long sessionId) {
        if (sessionId == null) {
            return;
        }
        UserSession session = userSessionRepository.findByIdAndUserIdAndRevokedFalse(sessionId, userId)
                .orElseThrow(() -> new AuthenticationException("Session not found"));
        revoke(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionDto> listVisibleSessions(Long userId) {
        List<UserSession> visibleSessions = findActiveSessions(userId).stream()
                .filter(this::isWithinActiveWindow)
                .toList();

        Map<String, UserSession> latestByKey = new LinkedHashMap<>();
        for (UserSession session : visibleSessions) {
            latestByKey.putIfAbsent(sessionFingerprint.displayKey(session), session);
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
