package com.crm.mcsv_auth.util;

import com.crm.mcsv_auth.config.CookieProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final CookieProperties cookieProperties;

    public ResponseCookie createAccessTokenCookie(String token) {
        return buildCookie("access_token", token, cookieProperties.getAccessTokenMaxAge());
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return buildCookie("refresh_token", token, cookieProperties.getRefreshTokenMaxAge());
    }

    public ResponseCookie clearAccessTokenCookie() {
        return buildCookie("access_token", "", 0);
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return buildCookie("refresh_token", "", 0);
    }

    private ResponseCookie buildCookie(String name, String value, long maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path(cookieProperties.getPath())
                .maxAge(maxAge)
                .sameSite(cookieProperties.getSameSite());

        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isBlank()) {
            builder.domain(cookieProperties.getDomain());
        }

        return builder.build();
    }
}
