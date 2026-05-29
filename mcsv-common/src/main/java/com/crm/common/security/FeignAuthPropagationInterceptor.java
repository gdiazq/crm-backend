package com.crm.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Propagates the caller's {@code Authorization} header onto outgoing Feign calls so that
 * service-to-service requests carry the original user's JWT and pass the downstream
 * {@link JwtAuthenticationFilter}.
 *
 * <p>No-op when there is no request context (e.g. scheduled/async work) or no inbound token,
 * which keeps pre-authentication flows (such as login lookups) working unchanged.
 */
public class FeignAuthPropagationInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        if (template.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
            return;
        }
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && !authorization.isBlank()) {
            template.header(HttpHeaders.AUTHORIZATION, authorization);
        }
    }
}
