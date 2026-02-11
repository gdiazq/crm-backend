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
import com.crm.mcsv_auth.service.AuthService;
import com.crm.mcsv_auth.service.MfaService;
import com.crm.mcsv_auth.service.RateLimiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication and authorization")
public class AuthController {

    private final AuthService authService;
    private final RateLimiterService rateLimiterService;
    private final MfaService mfaService;

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
    @Operation(summary = "Resend verification code", description = "Resend email verification code")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.resendVerificationCode(email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Verification code sent successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return access and refresh tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String deviceId = httpRequest.getHeader("X-Device-Id");
        rateLimiterService.checkRateLimit("login:" + ipAddress, LOGIN_LIMIT, WINDOW_SECONDS);
        AuthResponse response = authService.login(request, ipAddress, userAgent, deviceId);
        return ResponseEntity.ok(response);
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

    @GetMapping("/mfa/status")
    public ResponseEntity<MfaStatusResponse> mfaStatus(@RequestHeader("X-Username") String username) {
        var user = authService.getUserByUsername(username);
        return ResponseEntity.ok(MfaStatusResponse.builder()
                .enabled(mfaService.isMfaEnabled(user.getId()))
                .build());
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
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and revoke refresh token")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        boolean logoutAll = Boolean.TRUE.equals(request.getLogoutAll());
        authService.logout(request.getRefreshToken(), logoutAll);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");

        return ResponseEntity.ok(response);
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

    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate JWT access token")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        boolean isValid = authService.validateToken(token);

        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);

        return ResponseEntity.ok(response);
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
