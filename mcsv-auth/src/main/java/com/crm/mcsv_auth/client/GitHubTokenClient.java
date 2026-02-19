package com.crm.mcsv_auth.client;

import com.crm.mcsv_auth.dto.GitHubTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "github-token-client", url = "https://github.com")
public interface GitHubTokenClient {

    @PostMapping(value = "/login/oauth/access_token",
                 headers = {"Accept=application/json"})
    GitHubTokenResponse exchangeToken(
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code") String code,
            @RequestParam("redirect_uri") String redirectUri
    );
}
