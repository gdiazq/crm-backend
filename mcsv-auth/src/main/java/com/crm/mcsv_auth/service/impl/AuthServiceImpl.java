package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.client.EmailClient;
import com.crm.mcsv_auth.client.NotificationClient;
import com.crm.mcsv_auth.client.UserClient;
import com.crm.mcsv_auth.config.JwtConfig;
import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.dto.CreateUserInternalRequest;
import com.crm.mcsv_auth.dto.EmailRequest;
import com.crm.mcsv_auth.dto.SendNotificationRequest;
import com.crm.mcsv_auth.dto.ForgotPasswordRequest;
import com.crm.mcsv_auth.dto.LoginRequest;
import com.crm.mcsv_auth.dto.MfaStatusResponse;
import com.crm.mcsv_auth.dto.RefreshTokenRequest;
import com.crm.mcsv_auth.dto.RegisterRequest;
import com.crm.mcsv_auth.dto.ResetPasswordRequest;
import com.crm.mcsv_auth.dto.UserDTO;
import com.crm.mcsv_auth.dto.VerifyEmailRequest;
import com.crm.mcsv_auth.entity.EmailVerificationCode;
import com.crm.mcsv_auth.entity.PasswordResetToken;
import com.crm.mcsv_auth.entity.RefreshToken;
import com.crm.mcsv_auth.entity.UserSession;
import com.crm.mcsv_auth.exception.AuthenticationException;
import com.crm.mcsv_auth.exception.MfaRequiredException;
import com.crm.mcsv_auth.exception.TokenException;
import com.crm.mcsv_auth.repository.EmailVerificationCodeRepository;
import com.crm.mcsv_auth.repository.PasswordResetTokenRepository;
import com.crm.mcsv_auth.repository.UserSessionRepository;
import com.crm.mcsv_auth.service.AuthService;
import com.crm.mcsv_auth.service.MfaService;
import com.crm.mcsv_auth.service.TokenService;
import com.crm.mcsv_auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserClient userClient;
    private final EmailClient emailClient;
    private final NotificationClient notificationClient;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final UserSessionRepository userSessionRepository;
    private final MfaService mfaService;
    private final JwtConfig jwtConfig;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    public Map<String, String> register(RegisterRequest request) {
        log.info("Registration attempt for: {}", request.getUsername());

        // Crear request para mcsv-user (sin password del usuario, se usa placeholder)
        CreateUserInternalRequest createUserRequest = CreateUserInternalRequest.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(generatePlaceholderPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .roleIds(new HashSet<>(Set.of(1L))) // Asignar ROLE_USER por defecto
                .build();

        // Crear usuario en mcsv-user vía endpoint interno
        ResponseEntity<UserDTO> response = userClient.signUpUser(createUserRequest);

        if (response.getBody() == null) {
            throw new AuthenticationException("Failed to create user");
        }

        UserDTO user = response.getBody();

        // Generar código de verificación y enviar email
        String code = generateVerificationCode();
        saveVerificationCode(user.getId(), code);
        sendVerificationEmail(user.getEmail(), user.getUsername(), code);

        log.info("User registered successfully, verification email sent to: {}", user.getEmail());

        // Enviar notificación de bienvenida
        sendWelcomeNotification(user.getId(), user.getUsername());

        return Map.of("message", "Registration successful. Please check your email for the verification code.");
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent, String deviceId) {
        log.info("Login attempt for: {}", request.getEmail());

        // Validar credenciales
        boolean isValidCredentials = validateCredentials(request.getEmail(), request.getPassword());
        if (!isValidCredentials) {
            throw new AuthenticationException("Invalid username or password");
        }

        // Obtener usuario
        UserDTO user = getUserByUsernameOrEmail(request.getEmail());

        // Validar código MFA si está habilitado
        if (mfaService.isMfaEnabled(user.getId())) {
            if (request.getTotpCode() == null || request.getTotpCode().isBlank()) {
                throw new AuthenticationException("MFA code is required");
            }
            boolean mfaValid = mfaService.verifyTotp(user.getId(), request.getTotpCode());
            if (!mfaValid) {
                throw new AuthenticationException("Invalid MFA code");
            }
        }

        // Validar email verificado
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AuthenticationException("Email not verified. Please verify your email before logging in.");
        }

        // Validar usuario activo
        if (!Boolean.TRUE.equals(user.getStatus())) {
            throw new AuthenticationException("User account is disabled");
        }

        if (!user.getAccountNonLocked()) {
            throw new AuthenticationException("User account is locked");
        }

        // Extraer roles
        Set<String> roles = user.getRoles().stream()
                .map(UserDTO.RoleDTO::getName)
                .collect(Collectors.toSet());

        // Generar tokens
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), roles);
        RefreshToken refreshToken = tokenService.createRefreshToken(user.getId());

        log.info("User logged in successfully: {}", user.getUsername());

        // Revocar sesión anterior del mismo dispositivo
        if (deviceId != null && !deviceId.isBlank()) {
            userSessionRepository.findByUserIdAndDeviceIdAndRevokedFalse(user.getId(), deviceId)
                    .ifPresent(oldSession -> {
                        oldSession.setRevoked(true);
                        oldSession.setRevokedAt(LocalDateTime.now());
                        userSessionRepository.save(oldSession);
                    });
        }

        userSessionRepository.save(UserSession.builder()
                .userId(user.getId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceId(deviceId)
                .build());

        // Enviar notificación de bienvenida al login
        sendLoginNotification(user.getId(), user.getUsername());

        // Construir respuesta
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getPlainToken())
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpireAt() * 60)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .avatarUrl(extractAvatarUrl(user))
                        .roles(roles)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token request");

        // Validar refresh token
        RefreshToken refreshToken = tokenService.validateRefreshToken(request.getRefreshToken());

        // Obtener usuario
        UserDTO user = getUserById(refreshToken.getUserId());

        // Extraer roles
        Set<String> roles = user.getRoles().stream()
                .map(UserDTO.RoleDTO::getName)
                .collect(Collectors.toSet());

        // Generar nuevo access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), roles);

        // Rotar refresh token
        tokenService.revokeRefreshToken(request.getRefreshToken());
        RefreshToken newRefreshToken = tokenService.createRefreshToken(user.getId());

        log.info("Token refreshed successfully for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getPlainToken())
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpireAt() * 60)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .avatarUrl(extractAvatarUrl(user))
                        .roles(roles)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken, boolean logoutAll) {
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
            // Revocar la sesión más reciente del usuario
            userSessionRepository.findFirstByUserIdAndRevokedFalseOrderByCreatedAtDesc(token.getUserId())
                    .ifPresent(session -> {
                        session.setRevoked(true);
                        session.setRevokedAt(LocalDateTime.now());
                        userSessionRepository.save(session);
                    });
            log.info("User logged out successfully");
        }
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request");

        // Buscar usuario por email
        UserDTO user;
        try {
            user = getUserByEmail(request.getEmail());
        } catch (Exception e) {
            // No revelar si el email existe o no por seguridad
            log.warn("Forgot password requested for non-existent email");
            return;
        }

        // Invalidar códigos anteriores
        emailVerificationCodeRepository.deleteByUserIdAndUsedFalse(user.getId());

        // Generar código de verificación y enviar por email
        String code = generateVerificationCode();
        saveVerificationCode(user.getId(), code);
        sendVerificationEmail(user.getEmail(), user.getUsername(), code);

        log.info("Verification code sent for password reset to user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Reset password request");
        consumePasswordToken(request);
    }

    @Override
    @Transactional
    public void createPassword(ResetPasswordRequest request) {
        log.info("Create password request");
        consumePasswordToken(request);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        ResponseEntity<UserDTO> response = userClient.getUserByUsername(username);
        if (response.getBody() == null) {
            throw new AuthenticationException("User not found");
        }
        return response.getBody();
    }

    @Override
    @Transactional
    public void logoutDevice(Long userId, String deviceId) {
        userSessionRepository.findByUserIdAndDeviceIdAndRevokedFalse(userId, deviceId)
                .ifPresent(session -> {
                    session.setRevoked(true);
                    session.setRevokedAt(LocalDateTime.now());
                    userSessionRepository.save(session);
                });
    }

    @Override
    @Transactional
    public void logoutSession(Long userId, Long sessionId) {
        UserSession session = userSessionRepository.findByIdAndUserIdAndRevokedFalse(sessionId, userId)
                .orElseThrow(() -> new AuthenticationException("Session not found"));
        session.setRevoked(true);
        session.setRevokedAt(LocalDateTime.now());
        userSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.crm.mcsv_auth.dto.UserSessionDto> listActiveSessions(Long userId) {
        return userSessionRepository.findByUserIdAndRevokedFalse(userId)
                .stream()
                .map(session -> com.crm.mcsv_auth.dto.UserSessionDto.builder()
                        .id(session.getId())
                        .ipAddress(session.getIpAddress())
                        .userAgent(session.getUserAgent())
                        .deviceId(session.getDeviceId())
                        .createdAt(session.getCreatedAt())
                        .lastSeenAt(session.getLastSeenAt())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    private UserDTO getUserByUsernameOrEmail(String usernameOrEmail) {
        try {
            // Intentar obtener por username primero
            ResponseEntity<UserDTO> response = userClient.getUserByUsername(usernameOrEmail);
            if (response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.debug("User not found by username, trying email");
        }

        try {
            // Si no se encuentra, intentar por email
            ResponseEntity<UserDTO> response = userClient.getUserByEmail(usernameOrEmail);
            if (response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("User not found by email", e);
        }

        throw new AuthenticationException("Invalid username or password");
    }

    private UserDTO getUserByEmail(String email) {
        ResponseEntity<UserDTO> response = userClient.getUserByEmail(email);
        if (response.getBody() == null) {
            throw new AuthenticationException("User not found");
        }
        return response.getBody();
    }

    private UserDTO getUserById(Long id) {
        ResponseEntity<UserDTO> response = userClient.getUserById(id);
        if (response.getBody() == null) {
            throw new AuthenticationException("User not found");
        }
        return response.getBody();
    }

    @Override
    @Transactional
    public Map<String, String> verifyEmail(VerifyEmailRequest request) {
        log.info("Email verification attempt for: {}", request.getEmail());

        UserDTO user = getUserByEmail(request.getEmail());

        // Try local (mcsv-auth) verification codes first (register/forgot-password flow)
        java.util.Optional<EmailVerificationCode> localCode = emailVerificationCodeRepository
                .findByUserIdAndCodeAndUsedFalse(user.getId(), request.getCode());

        if (localCode.isPresent()) {
            EmailVerificationCode verificationCode = localCode.get();
            if (verificationCode.isExpired()) {
                throw new AuthenticationException("Verification code has expired. Please request a new one.");
            }
            verificationCode.setUsed(true);
            emailVerificationCodeRepository.save(verificationCode);
            log.info("Email verified via local code for: {}", request.getEmail());
            return completeEmailVerification(user.getId());
        }

        // Fallback: check codes stored in mcsv-user (admin-created user flow)
        try {
            ResponseEntity<Boolean> response = userClient.validateAndConsumeCode(
                    user.getId(), java.util.Map.of("code", request.getCode()));
            if (Boolean.TRUE.equals(response.getBody())) {
                log.info("Email verified via admin code for: {}", request.getEmail());
                return completeEmailVerification(user.getId());
            }
        } catch (Exception e) {
            log.error("Error calling validateAndConsumeCode on mcsv-user: {}", e.getMessage());
        }

        throw new AuthenticationException("Invalid verification code");
    }

    private Map<String, String> completeEmailVerification(Long userId) {
        userClient.verifyEmail(userId);

        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordToken = PasswordResetToken.builder()
                .token(token)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        passwordResetTokenRepository.save(passwordToken);

        return Map.of("message", "Email verified successfully", "token", token);
    }

    @Override
    @Transactional
    public void resendVerificationCode(String email, String phoneNumber) {
        log.info("Resend verification code request");

        UserDTO user;
        try {
            user = getUserByEmail(email);
        } catch (Exception e) {
            log.warn("Resend verification requested for non-existent email");
            return;
        }

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return;
        }

        // Validar que el teléfono coincida con el registrado
        if (user.getPhoneNumber() == null || !user.getPhoneNumber().equals(phoneNumber)) {
            log.warn("Resend verification: phone number mismatch");
            return;
        }

        // Invalidar códigos anteriores
        emailVerificationCodeRepository.deleteByUserIdAndUsedFalse(user.getId());

        // Generar nuevo código y enviar por email
        String code = generateVerificationCode();
        saveVerificationCode(user.getId(), code);
        sendVerificationEmail(user.getEmail(), user.getUsername(), code);
    }

    @Override
    public boolean checkEmailAvailability(String email) {
        try {
            ResponseEntity<UserDTO> response = userClient.getUserByEmail(email);
            return response.getBody() == null;
        } catch (Exception e) {
            return true;
        }
    }

    private String generateVerificationCode() {
        int code = SECURE_RANDOM.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private void saveVerificationCode(Long userId, String code) {
        EmailVerificationCode verificationCode = EmailVerificationCode.builder()
                .code(code)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
                .used(false)
                .build();

        emailVerificationCodeRepository.save(verificationCode);
    }

    private void sendVerificationEmail(String email, String username, String code) {
        try {
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            String link = frontendUrl + "/verify-email?email=" + encodedEmail + "&code=" + code;

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(email)
                    .subject("Verify your email address")
                    .templateName("verification-code")
                    .variables(Map.of(
                            "code", code,
                            "username", username,
                            "link", link
                    ))
                    .build();

            emailClient.sendEmail(emailRequest);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
        }
    }

    private void consumePasswordToken(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new TokenException("Invalid password reset token"));

        if (resetToken.getUsed()) {
            throw new TokenException("Password reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new TokenException("Password reset token has expired");
        }

        userClient.updatePassword(
                new UserClient.UpdatePasswordRequest(resetToken.getUserId(), request.getNewPassword())
        );

        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        log.info("Password updated successfully for user ID: {}", resetToken.getUserId());
    }

    private String generatePlaceholderPassword() {
        return "Tmp!" + UUID.randomUUID().toString();
    }

    private void sendWelcomeNotification(Long userId, String username) {
        try {
            notificationClient.send(SendNotificationRequest.builder()
                    .userId(userId)
                    .title("Bienvenido a CRM")
                    .message("Hola " + username + ", tu cuenta ha sido creada exitosamente. Verifica tu correo para comenzar.")
                    .type("SUCCESS")
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send welcome notification to userId: {}", userId, e);
        }
    }

    private void sendLoginNotification(Long userId, String username) {
        try {
            notificationClient.send(SendNotificationRequest.builder()
                    .userId(userId)
                    .title("Inicio de sesión")
                    .message("Bienvenido de vuelta, " + username + ". Has iniciado sesión exitosamente.")
                    .type("INFO")
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send login notification to userId: {}", userId, e);
        }
    }

    @Override
    public AuthResponse.UserInfo getCurrentUser(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new AuthenticationException("Invalid or expired token");
        }

        Long userId = jwtUtil.extractUserId(token);
        UserDTO user = getUserById(userId);

        Set<String> roles = user.getRoles().stream()
                .map(UserDTO.RoleDTO::getName)
                .collect(Collectors.toSet());

        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(extractAvatarUrl(user))
                .roles(roles)
                .build();
    }

    @Override
    public boolean checkMfaStatus(String email) {
        UserDTO user = getUserByUsernameOrEmail(email);
        return mfaService.isMfaEnabled(user.getId());
    }

    @Override
    public MfaStatusResponse getMfaStatusByEmail(String email) {
        UserDTO user = getUserByUsernameOrEmail(email);
        return mfaService.getMfaStatus(user.getId());
    }

    private String extractAvatarUrl(UserDTO user) {
        return user.getAvatarUrl();
    }

    private boolean validateCredentials(String usernameOrEmail, String password) {
        try {
            ResponseEntity<Boolean> response = userClient.validateCredentials(
                    new UserClient.CredentialsRequest(usernameOrEmail, password)
            );

            if (response.getBody() != null) {
                return response.getBody();
            }

            log.error("Error validating credentials - null response body");
            return false;
        } catch (Exception e) {
            log.error("Exception while validating credentials: {}", e.getMessage());
            return false;
        }
    }
}
