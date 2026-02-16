package com.crm.mcsv_auth.controller;

import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.dto.ForgotPasswordRequest;
import com.crm.mcsv_auth.dto.LoginRequest;
import com.crm.mcsv_auth.dto.MfaSetupRequest;
import com.crm.mcsv_auth.dto.MfaSetupResponse;
import com.crm.mcsv_auth.dto.MfaStatusResponse;
import com.crm.mcsv_auth.dto.MfaVerifyRequest;
import com.crm.mcsv_auth.dto.RefreshTokenRequest;
import com.crm.mcsv_auth.dto.ResetPasswordRequest;
import com.crm.mcsv_auth.dto.UserSessionDto;
import com.crm.mcsv_auth.dto.VerifyEmailRequest;
import com.crm.mcsv_auth.dto.WsTicketResponse;
import com.crm.mcsv_auth.service.AuthService;
import com.crm.mcsv_auth.service.MfaService;
import com.crm.mcsv_auth.service.RateLimiterService;
import com.crm.mcsv_auth.service.WsTicketService;
import com.crm.mcsv_auth.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication and authorization")
public class AuthController {

    private final AuthService authService;
    private final RateLimiterService rateLimiterService;
    private final MfaService mfaService;
    private final CookieUtil cookieUtil;
    private final WsTicketService wsTicketService;

    private static final int LOGIN_LIMIT = 5;
    private static final int FORGOT_PASSWORD_LIMIT = 3;
    private static final long WINDOW_SECONDS = 60;

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user and send email verification code")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody com.crm.mcsv_auth.dto.RegisterRequest request) {
        Map<String, String> response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email with 6-digit code and return password creation token")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        Map<String, String> response = authService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-password")
    @Operation(summary = "Create password", description = "Create password after email verification using one-time token")
    public ResponseEntity<Map<String, String>> createPassword(@Valid @RequestBody com.crm.mcsv_auth.dto.ResetPasswordRequest request) {
        authService.createPassword(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password created successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification code", description = "Resend email verification code after validating phone number")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String phoneNumber = request.get("phoneNumber");
        authService.resendVerificationCode(email, phoneNumber);
        Map<String, String> response = new HashMap<>();
        response.put("message", "If the account exists, a verification code has been sent");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email")
    @Operation(summary = "Check email availability", description = "Check if an email is available for registration")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean available = authService.checkEmailAvailability(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pre-login")
    @Operation(summary = "Pre-login check", description = "Check if user requires MFA before login")
    public ResponseEntity<Map<String, Boolean>> preLogin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        boolean mfaRequired = authService.checkMfaStatus(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("mfaRequired", mfaRequired);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return access and refresh tokens via cookies")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String deviceId = httpRequest.getHeader("X-Device-Id");
        rateLimiterService.checkRateLimit("login:" + ipAddress, LOGIN_LIMIT, WINDOW_SECONDS);
        AuthResponse response = authService.login(request, ipAddress, userAgent, deviceId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(response.getAccessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(response.getRefreshToken()).toString())
                .body(response);
    }

    @PostMapping("/mfa/setup")
    public ResponseEntity<MfaSetupResponse> setupMfa(
            @Valid @RequestBody MfaSetupRequest request,
            HttpServletRequest httpRequest
    ) {
        String deviceId = getRequiredDeviceId(httpRequest);
        var user = authService.getUserByUsername(request.getUsername());
        MfaSetupResponse response = mfaService.setupTotp(user.getId(), user.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<Map<String, Boolean>> verifyMfa(
            @Valid @RequestBody MfaVerifyRequest request,
            HttpServletRequest httpRequest
    ) {
        String deviceId = getRequiredDeviceId(httpRequest);
        var user = authService.getUserByUsername(request.getUsername());
        boolean isValid = mfaService.verifyTotp(user.getId(), request.getCode());
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mfa/disable")
    public ResponseEntity<Map<String, Boolean>> disableMfa(
            @Valid @RequestBody MfaSetupRequest request,
            HttpServletRequest httpRequest
    ) {
        String deviceId = getRequiredDeviceId(httpRequest);
        var user = authService.getUserByUsername(request.getUsername());
        mfaService.disableTotp(user.getId());
        Map<String, Boolean> response = new HashMap<>();
        response.put("disabled", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mfa/status/{email}")
    @Operation(summary = "MFA status", description = "Get MFA status, verification and last verification date for a user by email")
    public ResponseEntity<MfaStatusResponse> mfaStatus(@PathVariable String email) {
        MfaStatusResponse mfaStatus = authService.getMfaStatusByEmail(email);
        return ResponseEntity.ok(mfaStatus);
    }

    @PostMapping("/logout-device")
    public ResponseEntity<Map<String, String>> logoutDevice(
            @RequestHeader("X-Username") String username,
            HttpServletRequest httpRequest
    ) {
        String deviceId = getRequiredDeviceId(httpRequest);
        var user = authService.getUserByUsername(username);
        authService.logoutDevice(user.getId(), deviceId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Device logged out");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<UserSessionDto>> listSessions(@RequestHeader("X-Username") String username) {
        var user = authService.getUserByUsername(username);
        List<UserSessionDto> sessions = authService.listActiveSessions(user.getId());
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token from cookie")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        String refreshToken = refreshTokenCookie;
        if (refreshToken == null || refreshToken.isBlank()) {
            if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
                refreshToken = request.getRefreshToken();
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required");
            }
        }

        RefreshTokenRequest tokenRequest = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();
        AuthResponse response = authService.refreshToken(tokenRequest);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(response.getAccessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(response.getRefreshToken()).toString())
                .body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and revoke refresh token, clear cookies")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        String refreshToken = refreshTokenCookie;
        boolean logoutAll = false;

        if (refreshToken == null || refreshToken.isBlank()) {
            if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
                refreshToken = request.getRefreshToken();
            }
        }
        if (request != null && Boolean.TRUE.equals(request.getLogoutAll())) {
            logoutAll = true;
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken, logoutAll);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.clearAccessTokenCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookieUtil.clearRefreshTokenCookie().toString())
                .body(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset token")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = getClientIp(httpRequest);
        rateLimiterService.checkRateLimit("forgot-password:" + ipAddress, FORGOT_PASSWORD_LIMIT, WINDOW_SECONDS);
        authService.forgotPassword(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "If the email exists, a password reset link has been sent");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password with token")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get authenticated user info from JWT token (cookie or header)")
    public ResponseEntity<AuthResponse.UserInfo> me(
            @CookieValue(name = "access_token", required = false) String accessTokenCookie,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String token = resolveToken(accessTokenCookie, authHeader);
        AuthResponse.UserInfo userInfo = authService.getCurrentUser(token);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate JWT access token")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        boolean isValid = authService.validateToken(token);

        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ws-ticket")
    @Operation(summary = "Get WebSocket ticket", description = "Generate a single-use ticket for WebSocket authentication")
    public ResponseEntity<WsTicketResponse> getWsTicket(
            @CookieValue(name = "access_token", required = false) String accessTokenCookie,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String token = resolveToken(accessTokenCookie, authHeader);
        AuthResponse.UserInfo userInfo = authService.getCurrentUser(token);
        String ticket = wsTicketService.createTicket(userInfo.getId());
        return ResponseEntity.ok(WsTicketResponse.builder().ticket(ticket).build());
    }

    private String resolveToken(String accessTokenCookie, String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        if (accessTokenCookie != null && !accessTokenCookie.isBlank()) {
            return accessTokenCookie;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authentication token provided");
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getRequiredDeviceId(HttpServletRequest request) {
        String deviceId = request.getHeader("X-Device-Id");
        if (deviceId == null || deviceId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Device ID required for MFA");
        }
        return deviceId;
    }
}
