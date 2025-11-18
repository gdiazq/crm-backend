package com.crm.mcsv_user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtValidator {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtClaims validateAndExtractClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (claims.getExpiration().before(new Date())) {
                log.warn("Token expired");
                return null;
            }

            String username = claims.getSubject();
            Long userId = claims.get("userId", Long.class);

            Object rolesObj = claims.get("roles");
            Set<String> roles;

            if (rolesObj instanceof List) {
                roles = ((List<?>) rolesObj).stream()
                        .map(Object::toString)
                        .collect(Collectors.toSet());
            } else {
                roles = Set.of();
            }

            return new JwtClaims(userId, username, roles);

        } catch (Exception e) {
            log.error("Error validating JWT: {}", e.getMessage());
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JwtClaims {
        private Long userId;
        private String username;
        private Set<String> roles;
    }
}
