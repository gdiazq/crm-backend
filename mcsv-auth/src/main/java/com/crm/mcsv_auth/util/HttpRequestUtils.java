package com.crm.mcsv_auth.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Helpers to extract values from the incoming HTTP request consistently across controllers
 * (token, client IP, device id). Single source of truth so the parsing rules don't drift.
 */
public final class HttpRequestUtils {

    private static final String BEARER_PREFIX = "Bearer ";

    private HttpRequestUtils() {
    }

    /** Resolves the JWT from the Authorization header (Bearer) or the access_token cookie. */
    public static String resolveToken(String accessTokenCookie, String authHeader) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        if (accessTokenCookie != null && !accessTokenCookie.isBlank()) {
            return accessTokenCookie;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authentication token provided");
    }

    /** Real client IP, honouring X-Forwarded-For when behind the gateway/proxy. */
    public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /** Device id header, required for MFA flows. */
    public static String requiredDeviceId(HttpServletRequest request) {
        String deviceId = request.getHeader("X-Device-Id");
        if (deviceId == null || deviceId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Device ID required for MFA");
        }
        return deviceId;
    }
}
