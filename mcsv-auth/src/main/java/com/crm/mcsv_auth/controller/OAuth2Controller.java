package com.crm.mcsv_auth.controller;

import com.crm.mcsv_auth.config.GitHubOAuth2Config;
import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.service.GitHubOAuth2Service;
import com.crm.mcsv_auth.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2", description = "Social login endpoints")
public class OAuth2Controller {

    private final GitHubOAuth2Service gitHubOAuth2Service;
    private final CookieUtil cookieUtil;
    private final GitHubOAuth2Config gitHubOAuth2Config;

    @GetMapping("/github")
    @Operation(summary = "GitHub OAuth2 URL", description = "Returns the GitHub authorization URL to redirect the user")
    public ResponseEntity<Map<String, String>> githubAuthUrl() {
        String authUrl = gitHubOAuth2Service.buildAuthorizationUrl();
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    @GetMapping("/github/callback")
    @Operation(summary = "GitHub OAuth2 callback", description = "Exchanges the GitHub code for a JWT, sets cookies and redirects to frontend")
    public ResponseEntity<Void> githubCallback(
            @RequestParam("code") String code,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        String deviceId = httpRequest.getHeader("X-Device-Id");

        AuthResponse response = gitHubOAuth2Service.authenticate(code, ipAddress, userAgent, deviceId);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(response.getAccessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(response.getRefreshToken()).toString())
                .header(HttpHeaders.LOCATION, gitHubOAuth2Config.getFrontendSuccessUrl())
                .build();
    }
}
