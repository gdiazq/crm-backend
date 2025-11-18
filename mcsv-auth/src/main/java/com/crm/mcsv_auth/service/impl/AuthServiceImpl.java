package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.client.UserClient;
import com.crm.mcsv_auth.config.JwtConfig;
import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.dto.CreateUserInternalRequest;
import com.crm.mcsv_auth.dto.ForgotPasswordRequest;
import com.crm.mcsv_auth.dto.LoginRequest;
import com.crm.mcsv_auth.dto.RefreshTokenRequest;
import com.crm.mcsv_auth.dto.RegisterRequest;
import com.crm.mcsv_auth.dto.ResetPasswordRequest;
import com.crm.mcsv_auth.dto.UserDTO;
import com.crm.mcsv_auth.entity.PasswordResetToken;
import com.crm.mcsv_auth.entity.RefreshToken;
import com.crm.mcsv_auth.exception.AuthenticationException;
import com.crm.mcsv_auth.exception.TokenException;
import com.crm.mcsv_auth.repository.PasswordResetTokenRepository;
import com.crm.mcsv_auth.service.AuthService;
import com.crm.mcsv_auth.service.TokenService;
import com.crm.mcsv_auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserClient userClient;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtConfig jwtConfig;

    @Value("${internal.service.key:internal-microservice-key-2024}")
    private String internalServiceKey;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for: {}", request.getUsername());

        // Crear request para mcsv-user
        CreateUserInternalRequest createUserRequest = CreateUserInternalRequest.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .roleIds(new HashSet<>(Set.of(1L))) // Asignar ROLE_USER por defecto
                .build();

        // Crear usuario en mcsv-user vía endpoint interno
        ResponseEntity<UserDTO> response = userClient.createUserInternal(internalServiceKey, createUserRequest);

        if (response.getBody() == null) {
            throw new AuthenticationException("Failed to create user");
        }

        UserDTO user = response.getBody();

        // Extraer roles
        Set<String> roles = user.getRoles().stream()
                .map(UserDTO.RoleDTO::getName)
                .collect(Collectors.toSet());

        // Generar tokens
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), roles);
        RefreshToken refreshToken = tokenService.createRefreshToken(user.getId(), user.getUsername());

        log.info("User registered successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpireAt() * 60)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(roles)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getUsernameOrEmail());

        // Validar credenciales con el microservicio de usuarios
        boolean isValidCredentials = validateCredentials(request.getUsernameOrEmail(), request.getPassword());

        if (!isValidCredentials) {
            throw new AuthenticationException("Invalid username or password");
        }

        // Obtener usuario del microservicio de usuarios (solo para obtener datos, no contraseña)
        UserDTO user = getUserByUsernameOrEmail(request.getUsernameOrEmail());

        // Validar usuario activo
        if (!user.getEnabled()) {
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
        RefreshToken refreshToken = tokenService.createRefreshToken(user.getId(), user.getUsername());

        log.info("User logged in successfully: {}", user.getUsername());

        // Construir respuesta
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpireAt() * 60)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
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

        log.info("Token refreshed successfully for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpireAt() * 60)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(roles)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Logout request");
        tokenService.revokeRefreshToken(refreshToken);
        log.info("User logged out successfully");
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());

        // Buscar usuario por email
        UserDTO user;
        try {
            user = getUserByEmail(request.getEmail());
        } catch (Exception e) {
            // No revelar si el email existe o no por seguridad
            log.warn("Forgot password requested for non-existent email: {}", request.getEmail());
            return;
        }

        // Generar token de reseteo
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .expiresAt(expiresAt)
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // TODO: Enviar email con el token de reseteo
        log.info("Password reset token created for user: {}", user.getUsername());
        log.info("Reset token: {}", token); // En producción esto NO debe loguearse
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Reset password request");

        // Buscar token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new TokenException("Invalid password reset token"));

        // Validar token
        if (resetToken.getUsed()) {
            throw new TokenException("Password reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new TokenException("Password reset token has expired");
        }

        // TODO: Actualizar contraseña en el microservicio de usuarios
        // Esto requeriría un endpoint en mcsv-user para actualizar contraseñas

        // Marcar token como usado
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successfully for user ID: {}", resetToken.getUserId());
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    private UserDTO getUserByUsernameOrEmail(String usernameOrEmail) {
        try {
            // Intentar obtener por username primero
            ResponseEntity<UserDTO> response = userClient.getUserByUsername(internalServiceKey, usernameOrEmail);
            if (response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.debug("User not found by username, trying email");
        }

        try {
            // Si no se encuentra, intentar por email
            ResponseEntity<UserDTO> response = userClient.getUserByEmail(internalServiceKey, usernameOrEmail);
            if (response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("User not found by email", e);
        }

        throw new AuthenticationException("Invalid username or password");
    }

    private UserDTO getUserByEmail(String email) {
        ResponseEntity<UserDTO> response = userClient.getUserByEmail(internalServiceKey, email);
        if (response.getBody() == null) {
            throw new AuthenticationException("User not found");
        }
        return response.getBody();
    }

    private UserDTO getUserById(Long id) {
        ResponseEntity<UserDTO> response = userClient.getUserById(internalServiceKey, id);
        if (response.getBody() == null) {
            throw new AuthenticationException("User not found");
        }
        return response.getBody();
    }

    private boolean validateCredentials(String usernameOrEmail, String password) {
        try {
            ResponseEntity<Boolean> response = userClient.validateCredentials(
                    internalServiceKey,
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
