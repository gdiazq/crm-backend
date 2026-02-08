package com.cmr.mcsv_gateway.filter;

import com.cmr.mcsv_gateway.model.ValidationError;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private static final String ERROR_SERVICE_UNAVAILABLE = "Servicio no disponible";

    private final WebClient.Builder webClient;

    @Getter
    @Setter
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }

    public AuthFilter(WebClient.Builder webClient) {
        super(Config.class);
        this.webClient = webClient;
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

            if (checkExchangeHeader(exchange)) {
                logger.error("Authentication header not present");
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, ERROR_SERVICE_UNAVAILABLE));
            }

            if (checkExchangeHeaderToken(exchange)) {
                logger.error("Authentication header jwt invalid format");
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, ERROR_SERVICE_UNAVAILABLE));
            }

            return checkExchangeValidToken(exchange).flatMap(valid -> {
                if (!valid.isValid()) {
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, valid.getErrorMessage()));
                }
                return checkExchangeValidUrl(exchange).flatMap(validUrl -> {
                    if (!validUrl.isValid()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, validUrl.getErrorMessage()));
                    }
                    return chain.filter(exchange);
                });
            });
        });
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
                        .path("/validateToken")
                        .queryParam("jwt", tokenHeader)
                        .build())
                .retrieve()
                .bodyToMono(ValidationError.class)
                .onErrorResume(e -> {
                    logger.error("Exception during token validation", e);
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage()));
                });
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
                        .path("/validateTokenUrl")
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
