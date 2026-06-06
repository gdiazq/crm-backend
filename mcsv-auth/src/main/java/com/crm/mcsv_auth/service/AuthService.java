package com.crm.mcsv_auth.service;

import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.dto.ForgotPasswordRequest;
import com.crm.mcsv_auth.dto.LoginRequest;
import com.crm.mcsv_auth.dto.MfaSetupResponse;
import com.crm.mcsv_auth.dto.MfaStatusResponse;
import com.crm.mcsv_auth.dto.RefreshTokenRequest;
import com.crm.mcsv_auth.dto.RegisterRequest;
import com.crm.mcsv_auth.dto.ResetPasswordRequest;
import com.crm.mcsv_auth.dto.TokenValidationResponse;
import com.crm.mcsv_auth.dto.UserDTO;
import com.crm.mcsv_auth.dto.UserSessionDto;
import com.crm.mcsv_auth.dto.VerifyEmailRequest;

import java.util.List;
import java.util.Map;

public interface AuthService {

    Map<String, String> register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent, String deviceId);

    AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent, String deviceId);

    void logout(String refreshToken, boolean logoutAll, String ipAddress, String userAgent, String deviceId);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    boolean validateToken(String token);

    TokenValidationResponse validateTokenResult(String jwt);

    UserDTO getUserByUsername(String username);

    void logoutSession(Long userId, Long sessionId);

    List<UserSessionDto> listActiveSessions(Long userId);

    Map<String, String> verifyEmail(VerifyEmailRequest request);

    void createPassword(ResetPasswordRequest request);

    void resendVerificationCode(String email, String phoneNumber);

    AuthResponse.UserInfo getCurrentUser(String token);

    boolean checkMfaStatus(String email);

    MfaStatusResponse getMfaStatusByEmail(String email);

    MfaSetupResponse setupMfa(String username);

    boolean verifyMfa(String username, String code);

    void disableMfa(String username);
}
