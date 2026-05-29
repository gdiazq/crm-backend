package com.crm.mcsv_auth.config;

import com.crm.mcsv_auth.util.JwtUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Attaches a short-lived service token to outgoing calls to mcsv-user so that pre-login
 * lookups (login, sign-up, password reset, email verification) pass mcsv-user's JWT filter,
 * even though there is no authenticated user yet.
 *
 * <p>Scoped to the {@code mcsv-user} target only, leaving the GitHub OAuth clients untouched.
 * Skips when an Authorization header is already present (e.g. an authenticated request whose
 * user token was propagated), so the user's own token wins when available.
 */
@Component
@RequiredArgsConstructor
public class ServiceTokenFeignInterceptor implements RequestInterceptor {

    private static final String USER_SERVICE = "mcsv-user";

    private final JwtUtil jwtUtil;

    @Override
    public void apply(RequestTemplate template) {
        if (template.feignTarget() == null || !USER_SERVICE.equals(template.feignTarget().name())) {
            return;
        }
        if (template.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
            return;
        }
        template.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateServiceToken());
    }
}
