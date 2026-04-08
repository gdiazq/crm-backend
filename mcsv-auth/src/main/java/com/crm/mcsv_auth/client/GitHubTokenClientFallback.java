package com.crm.mcsv_auth.client;

import com.crm.mcsv_auth.dto.GitHubTokenResponse;
import org.springframework.stereotype.Component;

@Component
public class GitHubTokenClientFallback implements GitHubTokenClient {

    @Override
    public GitHubTokenResponse exchangeToken(String clientId, String clientSecret, String code, String redirectUri) {
        throw new GitHubTokenClientException("GitHub token service unavailable: cannot exchange OAuth code");
    }

    public static class GitHubTokenClientException extends RuntimeException {
        public GitHubTokenClientException(String message) {
            super(message);
        }
    }
}
