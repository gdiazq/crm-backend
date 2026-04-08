package com.crm.mcsv_auth.client;

import com.crm.mcsv_auth.dto.GitHubUserInfo;
import org.springframework.stereotype.Component;

@Component
public class GitHubApiClientFallback implements GitHubApiClient {

    @Override
    public GitHubUserInfo getUserInfo(String bearerToken) {
        throw new GitHubApiClientException("GitHub API service unavailable: cannot get user info");
    }

    public static class GitHubApiClientException extends RuntimeException {
        public GitHubApiClientException(String message) {
            super(message);
        }
    }
}
