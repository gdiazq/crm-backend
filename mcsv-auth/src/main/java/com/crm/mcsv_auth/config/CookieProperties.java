package com.crm.mcsv_auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cookie")
public class CookieProperties {

    private boolean secure = false;
    private String sameSite = "Lax";
    private String domain = "";
    private String path = "/";
    private long accessTokenMaxAge = 28800;   // 8 hours in seconds
    private long refreshTokenMaxAge = 604800; // 7 days in seconds
}
