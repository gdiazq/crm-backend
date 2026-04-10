package com.crm.mcsv_auth.client;

import com.crm.mcsv_auth.dto.GitHubTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GitHubTokenClientFallback implements GitHubTokenClient {

    @Override
    public GitHubTokenResponse exchangeToken(String clientId, String clientSecret, String code, String redirectUri) {
        log.warn("GitHub token exchange unavailable, returning error response");
        GitHubTokenResponse errorResponse = new GitHubTokenResponse();
        errorResponse.setError("service_unavailable");
        errorResponse.setErrorDescription("GitHub token service is temporarily unavailable");
        return errorResponse;
    }
}
