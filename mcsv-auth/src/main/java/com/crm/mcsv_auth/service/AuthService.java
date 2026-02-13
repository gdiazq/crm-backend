package com.crm.mcsv_auth.service;

import com.crm.mcsv_auth.dto.AuthResponse;
import com.crm.mcsv_auth.dto.ForgotPasswordRequest;
import com.crm.mcsv_auth.dto.LoginRequest;
import com.crm.mcsv_auth.dto.RefreshTokenRequest;
import com.crm.mcsv_auth.dto.RegisterRequest;
import com.crm.mcsv_auth.dto.ResetPasswordRequest;
import com.crm.mcsv_auth.dto.VerifyEmailRequest;

import java.util.Map;

public interface AuthService {

    Map<String, String> register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent, String deviceId);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken, boolean logoutAll);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    boolean validateToken(String token);

    com.crm.mcsv_auth.dto.UserDTO getUserByUsername(String username);

    void logoutDevice(Long userId, String deviceId);

    java.util.List<com.crm.mcsv_auth.dto.UserSessionDto> listActiveSessions(Long userId);

    Map<String, String> verifyEmail(VerifyEmailRequest request);

    void createPassword(ResetPasswordRequest request);

    void resendVerificationCode(String email, String phoneNumber);

    boolean checkEmailAvailability(String email);

    AuthResponse.UserInfo getCurrentUser(String token);
}
