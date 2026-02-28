package com.cmr.mcsv_gateway.filter;

import com.cmr.mcsv_gateway.config.JwtConfig;
import com.cmr.mcsv_gateway.model.ValidationError;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private static final String ERROR_SERVICE_UNAVAILABLE = "Servicio no disponible";
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private static final Map<String, String> PERMISSION_MAP = Map.ofEntries(
            Map.entry("POST:/v1/api/user/create",       "USER:CREATE"),
            Map.entry("GET:/v1/api/user/paged",         "USER:READ"),
            Map.entry("GET:/v1/api/user/detail",        "USER:READ"),
            Map.entry("PUT:/v1/api/user/update",        "USER:UPDATE"),
            Map.entry("PUT:/v1/api/user/status",        "USER:UPDATE"),
            Map.entry("GET:/v1/api/role/paged",         "ROLE:READ"),
            Map.entry("POST:/v1/api/role/create",       "ROLE:CREATE"),
            Map.entry("PUT:/v1/api/role/update",        "ROLE:UPDATE"),
            Map.entry("PUT:/v1/api/role/status",        "ROLE:UPDATE"),
            Map.entry("DELETE:/v1/api/role",            "ROLE:DELETE"),
            Map.entry("POST:/v1/api/rrhh/employee/create",          "EMPLOYEE:CREATE"),
            Map.entry("GET:/v1/api/rrhh/employee/paged",            "EMPLOYEE:READ"),
            Map.entry("GET:/v1/api/rrhh/employee/detail",           "EMPLOYEE:READ"),
            Map.entry("GET:/v1/api/rrhh/employee/select",           "EMPLOYEE:READ"),
            Map.entry("PUT:/v1/api/rrhh/employee/update",           "EMPLOYEE:UPDATE"),
            Map.entry("PUT:/v1/api/rrhh/employee",                  "EMPLOYEE:UPDATE"),
            Map.entry("DELETE:/v1/api/rrhh/employee",               "EMPLOYEE:DELETE")
    );

    private final WebClient.Builder webClient;
    private final JwtConfig jwtConfig;

    @Getter
    @Setter
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }

    public AuthFilter(WebClient.Builder webClient, JwtConfig jwtConfig) {
        super(Config.class);
        this.webClient = webClient;
        this.jwtConfig = jwtConfig;
        logger.info("AuthFilter");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {

            exchange.getRequest().getHeaders().forEach(
                    (key, value) -> logger.debug("Header: {} = {}", key, value)
            );

            if (exchange.getRequest().getPath().toString().startsWith("/upload/img")) {
                return chain.filter(exchange);
            }

            ServerWebExchange mutatedExchange = injectAuthFromCookieIfNeeded(exchange);

            if (checkExchangeHeader(mutatedExchange)) {
                logger.error("Authentication header not present");
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, ERROR_SERVICE_UNAVAILABLE));
            }

            if (checkExchangeHeaderToken(mutatedExchange)) {
                logger.error("Authentication header jwt invalid format");
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, ERROR_SERVICE_UNAVAILABLE));
            }

            return checkExchangeValidToken(mutatedExchange).flatMap(valid -> {
                if (!valid.isValid()) {
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, valid.getErrorMessage()));
                }

                // Local permission check using JWT claims — no extra DB call
                String method = mutatedExchange.getRequest().getMethod().name();
                String urlPath = getUrlPath(mutatedExchange);
                if (!checkPermission(mutatedExchange, method, urlPath)) {
                    logger.warn("Permission denied for {} {}", method, urlPath);
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions"));
                }

                return checkExchangeValidUrl(mutatedExchange).flatMap(validUrl -> {
                    if (!validUrl.isValid()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, validUrl.getErrorMessage()));
                    }
                    ServerWebExchange enrichedExchange = injectUserIdHeader(mutatedExchange);
                    return chain.filter(enrichedExchange);
                });
            });
        });
    }

    /**
     * Returns true if the request is allowed to proceed.
     * Matches the route against PERMISSION_MAP by prefix. If the route requires a permission
     * that the JWT doesn't contain → returns false. If the route is not in the map → pass.
     */
    private boolean checkPermission(ServerWebExchange exchange, String method, String urlPath) {
        Set<String> userPermissions = extractPermissions(exchange);

        for (Map.Entry<String, String> entry : PERMISSION_MAP.entrySet()) {
            String[] parts = entry.getKey().split(":", 2);
            String mapMethod = parts[0];
            String mapPath   = parts[1];

            if (method.equalsIgnoreCase(mapMethod) && urlPath.startsWith(mapPath)) {
                String required = entry.getValue();
                boolean hasPermission = userPermissions.contains(required);
                if (!hasPermission) {
                    logger.warn("Missing permission '{}' for {} {}", required, method, urlPath);
                }
                return hasPermission;
            }
        }
        // Route not in map — any authenticated user can access
        return true;
    }

    private Set<String> extractPermissions(ServerWebExchange exchange) {
        try {
            String tokenHeader = getTokenHeader(exchange);
            String token = tokenHeader.substring(7); // remove "Bearer "
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object perms = claims.get("permissions");
            if (perms instanceof List) {
                return new HashSet<>((List<String>) perms);
            }
        } catch (Exception e) {
            logger.warn("Could not extract permissions from token: {}", e.getMessage());
        }
        return new HashSet<>();
    }

    private ServerWebExchange injectAuthFromCookieIfNeeded(ServerWebExchange exchange) {
        if (exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return exchange;
        }

        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(ACCESS_TOKEN_COOKIE);
        if (cookie == null || cookie.getValue().isBlank()) {
            return exchange;
        }

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + cookie.getValue())
                .build();

        return exchange.mutate().request(mutatedRequest).build();
    }

    private boolean checkExchangeHeader(ServerWebExchange exchange) {
        return !exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
    }

    private boolean checkExchangeHeaderToken(ServerWebExchange exchange) {
        String tokenHeader = getTokenHeader(exchange);
        String[] chunks = tokenHeader.split(" ");
        return chunks.length != 2 || !chunks[0].equals("Bearer");
    }

    private String getTokenHeader(ServerWebExchange exchange) {
        String tokenHeader = "";
        List<String> headers = exchange
                .getRequest()
                .getHeaders()
                .get(HttpHeaders.AUTHORIZATION);
        if (headers != null && !headers.isEmpty()) {
            tokenHeader = headers.get(0);
        }
        return tokenHeader;
    }

    private String getUrlPath(ServerWebExchange exchange) {
        LinkedHashSet<URI> attr = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        URI option = Objects.requireNonNull(attr)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, ERROR_SERVICE_UNAVAILABLE));
        return option.getPath();
    }

    private Mono<ValidationError> checkExchangeValidToken(ServerWebExchange exchange) {
        String tokenHeader = getTokenHeader(exchange);
        return webClient.build()
                .post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("lb")
                        .host("mcsv-auth")
                        .path("/v1/validateToken")
                        .queryParam("jwt", tokenHeader)
                        .build())
                .retrieve()
                .bodyToMono(ValidationError.class)
                .onErrorResume(e -> {
                    logger.error("Exception during token validation", e);
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage()));
                });
    }

    private ServerWebExchange injectUserIdHeader(ServerWebExchange exchange) {
        try {
            String tokenHeader = getTokenHeader(exchange);
            String token = tokenHeader.substring(7); // remove "Bearer "
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object userId = claims.get("userId");
            if (userId != null) {
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .headers(headers -> headers.remove("X-User-Id")) // eliminar cualquier valor del cliente
                        .header("X-User-Id", userId.toString())
                        .build();
                return exchange.mutate().request(mutatedRequest).build();
            }
        } catch (Exception e) {
            logger.warn("Could not extract userId from token: {}", e.getMessage());
        }
        return exchange;
    }

    private Mono<ValidationError> checkExchangeValidUrl(ServerWebExchange exchange) {
        String token = getTokenHeader(exchange);
        String method = exchange.getRequest().getMethod().name();
        String urlPath = getUrlPath(exchange);

        return webClient.build()
                .post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("lb")
                        .host("mcsv-auth")
                        .path("/v1/validateTokenUrl")
                        .queryParam("jwt", token)
                        .queryParam("urlPath", urlPath)
                        .queryParam("method", method)
                        .build())
                .retrieve()
                .bodyToMono(ValidationError.class)
                .onErrorResume(e -> {
                    logger.error("Exception during url validation", e);
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage()));
                });
    }
}
