package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.client.UserClient;
import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.dto.CredentialsRequest;
import com.crm.mcsv_auth.dto.CreateUserInternalRequest;
import com.crm.mcsv_auth.dto.ForgotPasswordRequest;
import com.crm.mcsv_auth.dto.LoginRequest;
import com.crm.mcsv_auth.dto.MfaSetupResponse;
import com.crm.mcsv_auth.dto.MfaStatusResponse;
import com.crm.mcsv_auth.dto.RefreshTokenRequest;
import com.crm.mcsv_auth.dto.RegisterRequest;
import com.crm.mcsv_auth.dto.ResetPasswordRequest;
import com.crm.mcsv_auth.dto.UserDTO;
import com.crm.mcsv_auth.dto.UserSessionDto;
import com.crm.mcsv_auth.dto.VerifyEmailRequest;
import com.crm.mcsv_auth.entity.RefreshToken;
import com.crm.mcsv_auth.entity.UserSession;
import com.crm.mcsv_auth.exception.AuthenticationException;
import com.crm.mcsv_auth.mapper.AuthResponseMapper;
import com.crm.mcsv_auth.repository.UserSessionRepository;
import com.crm.mcsv_auth.service.AuthNotificationService;
import com.crm.mcsv_auth.service.AuthService;
import com.crm.mcsv_auth.service.AuthTokenResponseService;
import com.crm.mcsv_auth.service.AuthUserLookupService;
import com.crm.mcsv_auth.service.EmailVerificationCodeService;
import com.crm.mcsv_auth.service.EmailVerificationCompletionService;
import com.crm.mcsv_auth.service.MfaService;
import com.crm.mcsv_auth.service.PasswordTokenConsumptionService;
import com.crm.mcsv_auth.service.TokenService;
import com.crm.mcsv_auth.service.VerificationEmailService;
import com.crm.mcsv_auth.service.UserSessionManager;
import com.crm.mcsv_auth.util.AuthCredentialUtil;
import com.crm.mcsv_auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserClient userClient;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final UserSessionRepository userSessionRepository;
    private final MfaService mfaService;
    private final UserSessionManager userSessionManager;
    private final AuthTokenResponseService authTokenResponseService;
    private final AuthResponseMapper authResponseMapper;
    private final AuthUserLookupService authUserLookupService;
    private final EmailVerificationCodeService emailVerificationCodeService;
    private final EmailVerificationCompletionService emailVerificationCompletionService;
    private final VerificationEmailService verificationEmailService;
    private final PasswordTokenConsumptionService passwordTokenConsumptionService;
    private final AuthNotificationService authNotificationService;

    @Override
    @Transactional
    public Map<String, String> register(RegisterRequest request) {
        log.info("Registration attempt for: {}", request.getUsername());

        CreateUserInternalRequest createUserRequest = CreateUserInternalRequest.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(AuthCredentialUtil.generatePlaceholderPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .roleIds(new HashSet<>(Set.of(1L))) // Asignar ROLE_USER por defecto
                .build();

        ResponseEntity<UserDTO> response = userClient.signUpUser(createUserRequest);

        if (response.getBody() == null) {
            throw new AuthenticationException("Failed to create user");
        }

        UserDTO user = response.getBody();

        String code = emailVerificationCodeService.createCode(user.getId());
        verificationEmailService.sendVerificationEmail(user.getEmail(), user.getUsername(), code);

        log.info("User registered successfully, verification email sent to: {}", user.getEmail());

        authNotificationService.sendWelcomeNotification(user.getId(), user.getUsername());

        return Map.of("message",
                "Registration successful. Please check your email for the verification code.");
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent, String deviceId) {
        log.info("Login attempt for: {}", request.getEmail());

        final Boolean body;
        try {
            body = userClient.validateCredentials(
                    new CredentialsRequest(request.getEmail(), request.getPassword())
            ).getBody();
        } catch (Exception e) {
            log.error("Exception while validating credentials: {}", e.getMessage());
            throw new AuthenticationException("Invalid username or password");
        }
        if (!Boolean.TRUE.equals(body)) {
            if (body == null) {
                log.error("Error validating credentials - null response body");
            }
            throw new AuthenticationException("Invalid username or password");
        }

        UserDTO user = authUserLookupService.getByUsernameOrEmail(request.getEmail());

        if (mfaService.isMfaEnabled(user.getId())) {
            if (request.getTotpCode() == null || request.getTotpCode().isBlank()) {
                throw new AuthenticationException("MFA code is required");
            }
            boolean mfaValid = mfaService.verifyTotp(user.getId(), request.getTotpCode());
            if (!mfaValid) {
                throw new AuthenticationException("Invalid MFA code");
            }
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AuthenticationException("Email not verified. Please verify your email before logging in.");
        }

        if (!Boolean.TRUE.equals(user.getStatus())) {
            throw new AuthenticationException("User account is disabled");
        }

        if (!user.getAccountNonLocked()) {
            throw new AuthenticationException("User account is locked");
        }

        AuthResponse authResponse = authTokenResponseService.createSessionResponse(user, ipAddress, userAgent, deviceId);

        log.info("User logged in successfully: {}", user.getUsername());
        authNotificationService.sendLoginNotification(user.getId(), user.getUsername());

        return authResponse;
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent, String deviceId) {
        log.info("Refresh token request");

        RefreshToken refreshToken = tokenService.validateRefreshToken(request.getRefreshToken());

        UserDTO user = authUserLookupService.getById(refreshToken.getUserId());

        tokenService.revokeRefreshToken(request.getRefreshToken());
        UserSession session = userSessionManager.attachSession(
                user.getId(),
                refreshToken.getSessionId(),
                ipAddress,
                userAgent,
                deviceId);
        String newAccessToken = authTokenResponseService.createAccessToken(user, session.getId());
        RefreshToken newRefreshToken = tokenService.createRefreshToken(user.getId(), session.getId());

        log.info("Token refreshed successfully for user: {}", user.getUsername());

        return authResponseMapper.toAuthResponse(newAccessToken, newRefreshToken.getPlainToken(), user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken, boolean logoutAll, String ipAddress, String userAgent, String deviceId) {
        log.info("Logout request");
        RefreshToken token = tokenService.validateRefreshToken(refreshToken);
        if (logoutAll) {
            tokenService.revokeAllUserTokens(token.getUserId());
            var sessions = userSessionRepository.findByUserId(token.getUserId());
            LocalDateTime now = LocalDateTime.now();
            sessions.forEach(session -> {
                session.setRevoked(true);
                session.setRevokedAt(now);
            });
            userSessionRepository.saveAll(sessions);
            log.info("User logged out from all devices");
        } else {
            tokenService.revokeRefreshToken(refreshToken);
            if (token.getSessionId() != null) {
                tokenService.revokeSessionTokens(token.getSessionId());
                userSessionManager.revokeSessionExact(token.getUserId(), token.getSessionId());
            } else {
                userSessionManager.revokeCurrentSession(token.getUserId(), ipAddress, userAgent, deviceId);
            }
            log.info("User logged out successfully");
        }
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request");

        UserDTO user;
        try {
            user = authUserLookupService.getByEmail(request.getEmail());
        } catch (Exception e) {
            log.warn("Forgot password requested for non-existent email: {}", request.getEmail());
            throw new AuthenticationException("No account found with that email address");
        }

        emailVerificationCodeService.deleteUnusedCodes(user.getId());

        String code = emailVerificationCodeService.createCode(user.getId());
        verificationEmailService.sendVerificationEmail(user.getEmail(), user.getUsername(), code);

        log.info("Verification code sent for password reset to user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Reset password request");
        passwordTokenConsumptionService.consume(request);
    }

    @Override
    @Transactional
    public void createPassword(ResetPasswordRequest request) {
        log.info("Create password request");
        passwordTokenConsumptionService.consume(request);
    }

    @Override
    public boolean validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            return false;
        }
        try {
            Long sessionId = jwtUtil.extractSessionId(token);
            // Backward-compat: tokens issued before session binding carry no sessionId.
            if (sessionId == null) {
                return true;
            }
            Long userId = jwtUtil.extractUserId(token);
            return userSessionRepository
                    .findByIdAndUserIdAndRevokedFalseAndExpiresAtAfter(sessionId, userId, LocalDateTime.now())
                    .isPresent();
        } catch (Exception e) {
            log.error("Error validating session bound to token", e);
            return false;
        }
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        return authUserLookupService.getByUsername(username);
    }

    @Override
    @Transactional
    public void logoutSession(Long userId, Long sessionId) {
        tokenService.revokeSessionTokens(sessionId);
        userSessionManager.revokeSessionExact(userId, sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionDto> listActiveSessions(Long userId) {
        return userSessionManager.listVisibleSessions(userId);
    }

    @Override
    @Transactional
    public Map<String, String> verifyEmail(VerifyEmailRequest request) {
        log.info("Email verification attempt for: {}", request.getEmail());

        UserDTO user = authUserLookupService.getByEmail(request.getEmail());

        if (emailVerificationCodeService.validateAndConsume(user.getId(), request.getCode())) {
            log.info("Email verified for: {}", request.getEmail());
            return emailVerificationCompletionService.complete(user.getId());
        }

        throw new AuthenticationException("Invalid verification code");
    }

    @Override
    @Transactional
    public void resendVerificationCode(String email, String phoneNumber) {
        log.info("Resend verification code request");

        UserDTO user;
        try {
            user = authUserLookupService.getByEmail(email);
        } catch (Exception e) {
            log.warn("Resend verification requested for non-existent email");
            return;
        }

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return;
        }

        if (user.getPhoneNumber() == null || !user.getPhoneNumber().equals(phoneNumber)) {
            log.warn("Resend verification: phone number mismatch");
            return;
        }

        emailVerificationCodeService.deleteUnusedCodes(user.getId());

        String code = emailVerificationCodeService.createCode(user.getId());
        verificationEmailService.sendVerificationEmail(user.getEmail(), user.getUsername(), code);
    }

    @Override
    public AuthResponse.UserInfo getCurrentUser(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new AuthenticationException("Invalid or expired token");
        }

        Long userId = jwtUtil.extractUserId(token);
        UserDTO user = authUserLookupService.getById(userId);

        return authResponseMapper.toCurrentUserInfo(user);
    }

    @Override
    public boolean checkMfaStatus(String email) {
        UserDTO user = authUserLookupService.getByUsernameOrEmail(email);
        return mfaService.isMfaEnabled(user.getId());
    }

    @Override
    public MfaStatusResponse getMfaStatusByEmail(String email) {
        UserDTO user = authUserLookupService.getByUsernameOrEmail(email);
        return mfaService.getMfaStatus(user.getId());
    }

    @Override
    public MfaSetupResponse setupMfa(String username) {
        UserDTO user = getUserByUsername(username);
        return mfaService.setupTotp(user.getId(), user.getUsername());
    }

    @Override
    public boolean verifyMfa(String username, String code) {
        UserDTO user = getUserByUsername(username);
        return mfaService.verifyTotp(user.getId(), code);
    }

    @Override
    public void disableMfa(String username) {
        UserDTO user = getUserByUsername(username);
        mfaService.disableTotp(user.getId());
    }

}
