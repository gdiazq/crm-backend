package com.crm.mcsv_auth.client;

import com.crm.mcsv_auth.dto.GitHubUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GitHubApiClientFallback implements FallbackFactory<GitHubApiClient> {

    @Override
    public GitHubApiClient create(Throwable cause) {
        return (bearerToken) -> {
            log.error("GitHub API call failed. Cause: {} - {}", cause.getClass().getSimpleName(), cause.getMessage(), cause);
            return null;
        };
    }
}
