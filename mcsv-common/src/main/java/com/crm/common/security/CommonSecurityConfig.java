package com.crm.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@EnableConfigurationProperties(JwtSecurityProperties.class)
public class CommonSecurityConfig {

    /**
     * Inbound JWT validation filter. Registered only when the service opts in via
     * {@code app.security.jwt.enabled=true} (default off, so mcsv-auth's public endpoints
     * are unaffected). Runs early, before the dispatcher.
     */
    @Bean
    @ConditionalOnProperty(name = "app.security.jwt.enabled", havingValue = "true")
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilter(
            @Value("${jwt.secret}") String secret,
            JwtSecurityProperties properties,
            ObjectMapper objectMapper) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(new JwtAuthenticationFilter(secret, properties, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }

    /**
     * Propagates the caller's Authorization header on outgoing Feign calls. Active whenever
     * Feign is on the classpath; harmless (no-op) when there is no token to forward.
     */
    @Bean
    @ConditionalOnClass(name = "feign.RequestInterceptor")
    public FeignAuthPropagationInterceptor feignAuthPropagationInterceptor() {
        return new FeignAuthPropagationInterceptor();
    }
}
