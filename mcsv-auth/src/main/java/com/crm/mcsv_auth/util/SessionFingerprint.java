package com.crm.mcsv_auth.util;

import com.crm.mcsv_auth.entity.UserSession;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Identifies and de-duplicates user sessions by device id (preferred) or by a hashed
 * ip + user-agent fingerprint. Stateless and free of persistence, so it can be unit-tested
 * in isolation.
 */
@Component
public class SessionFingerprint {

    /** Keys that identify the caller right now (device key if present, plus the fingerprint key). */
    public Set<String> candidateKeys(String deviceId, String ipAddress, String userAgent) {
        Set<String> keys = new LinkedHashSet<>();
        if (!isBlank(deviceId)) {
            keys.add(deviceKey(deviceId));
        }
        keys.add(fingerprintKey(ipAddress, userAgent));
        return keys;
    }

    /** True if the stored session matches any of the candidate keys (same device or same fingerprint). */
    public boolean matches(UserSession session, Set<String> candidateKeys) {
        return candidateKeys.contains(deviceKey(session.getDeviceId()))
                || candidateKeys.contains(fingerprintKey(session.getIpAddress(), session.getUserAgent()));
    }

    /** Key used to collapse equivalent sessions into a single visible entry. */
    public String displayKey(UserSession session) {
        return !isBlank(session.getDeviceId())
                ? deviceKey(session.getDeviceId())
                : fingerprintKey(session.getIpAddress(), session.getUserAgent());
    }

    private String deviceKey(String deviceId) {
        return isBlank(deviceId) ? "" : "device:" + deviceId.trim().toLowerCase();
    }

    private String fingerprintKey(String ipAddress, String userAgent) {
        String raw = normalize(ipAddress) + "|" + normalize(userAgent);
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
