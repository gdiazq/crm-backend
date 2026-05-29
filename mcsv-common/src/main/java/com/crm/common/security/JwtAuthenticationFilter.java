package com.crm.common.security;

import com.crm.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Defense-in-depth resource-server filter. Each service that sits behind the gateway validates
 * the JWT locally so it is self-protecting even if reached directly (bypassing the gateway), and
 * derives {@code X-User-Id} from the verified token instead of trusting the inbound header.
 *
 * <p>Only active when {@code app.security.jwt.enabled=true}; see {@link JwtSecurityProperties}.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<String> ALWAYS_EXCLUDED = List.of("/actuator/**", "/error");

    private final SecretKey signingKey;
    private final JwtSecurityProperties properties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(String secret, JwtSecurityProperties properties, ObjectMapper objectMapper) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        for (String pattern : ALWAYS_EXCLUDED) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        for (String pattern : properties.getExcludePaths()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(request, response, "Missing or malformed Authorization header");
            return;
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(header.substring(BEARER_PREFIX.length()))
                    .getPayload();

            Object userId = claims.get("userId");
            chain.doFilter(new TrustedUserIdRequest(request, userId == null ? null : userId.toString()), response);
        } catch (Exception e) {
            log.warn("Rejected request to {}: invalid token ({})", request.getServletPath(), e.getMessage());
            writeUnauthorized(request, response, "Invalid or expired token");
        }
    }

    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        objectMapper.writeValue(response.getWriter(), body);
    }

    /**
     * Forces {@code X-User-Id} to the value extracted from the verified token, ignoring any
     * client- or gateway-supplied value so downstream controllers cannot be spoofed.
     */
    private static final class TrustedUserIdRequest extends HttpServletRequestWrapper {

        private final String userId;

        private TrustedUserIdRequest(HttpServletRequest request, String userId) {
            super(request);
            this.userId = userId;
        }

        @Override
        public String getHeader(String name) {
            if (USER_ID_HEADER.equalsIgnoreCase(name)) {
                return userId;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (USER_ID_HEADER.equalsIgnoreCase(name)) {
                return userId == null ? Collections.emptyEnumeration()
                        : Collections.enumeration(List.of(userId));
            }
            return super.getHeaders(name);
        }
    }
}
