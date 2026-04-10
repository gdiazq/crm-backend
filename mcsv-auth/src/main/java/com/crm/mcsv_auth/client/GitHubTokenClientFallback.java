package com.crm.mcsv_auth.client;

import com.crm.mcsv_auth.dto.GitHubTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GitHubTokenClientFallback implements FallbackFactory<GitHubTokenClient> {

    @Override
    public GitHubTokenClient create(Throwable cause) {
        return (clientId, clientSecret, code, redirectUri) -> {
            log.error("GitHub token exchange failed. Cause: {} - {}", cause.getClass().getSimpleName(), cause.getMessage(), cause);
            GitHubTokenResponse errorResponse = new GitHubTokenResponse();
            errorResponse.setError("service_unavailable");
            errorResponse.setErrorDescription("GitHub token service is temporarily unavailable");
            return errorResponse;
        };
    }
}
