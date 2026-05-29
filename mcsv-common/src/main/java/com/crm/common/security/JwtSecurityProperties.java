package com.crm.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the shared JWT resource-server filter.
 *
 * <p>Disabled by default so that modules scanning {@code com.crm.common} which must stay
 * publicly reachable (e.g. mcsv-auth login/refresh) are not affected. Enable it explicitly
 * per service that sits behind the gateway via {@code app.security.jwt.enabled=true}.
 */
@Data
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtSecurityProperties {

    /** Whether the inbound JWT validation filter is active for this service. */
    private boolean enabled = false;

    /**
     * Ant-style paths that bypass JWT validation (matched against the servlet path).
     * Infra paths are always excluded; this list is for service-specific internal endpoints
     * that legitimately receive token-less traffic (e.g. mcsv-auth pre-login lookups).
     */
    private List<String> excludePaths = new ArrayList<>();
}
