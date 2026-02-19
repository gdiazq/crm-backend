package com.crm.mcsv_auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "github.oauth2")
@Data
public class GitHubOAuth2Config {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scope = "user:email";
}
