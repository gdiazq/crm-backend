package com.crm.mcsv_auth.client;

import com.crm.mcsv_auth.dto.GitHubUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GitHubApiClientFallback implements GitHubApiClient {

    @Override
    public GitHubUserInfo getUserInfo(String bearerToken) {
        log.warn("GitHub API unavailable, returning null user info");
        return null;
    }
}
