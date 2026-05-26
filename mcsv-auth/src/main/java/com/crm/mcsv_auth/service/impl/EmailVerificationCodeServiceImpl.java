package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.client.UserClient;
import com.crm.mcsv_auth.entity.EmailVerificationCode;
import com.crm.mcsv_auth.exception.AuthenticationException;
import com.crm.mcsv_auth.repository.EmailVerificationCodeRepository;
import com.crm.mcsv_auth.service.EmailVerificationCodeService;
import com.crm.mcsv_auth.util.AuthCredentialUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationCodeServiceImpl implements EmailVerificationCodeService {

    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 10;

    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final UserClient userClient;

    @Override
    @Transactional
    public String createCode(Long userId) {
        String code = AuthCredentialUtil.generateVerificationCode();
        EmailVerificationCode verificationCode = EmailVerificationCode.builder()
                .code(code)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
                .used(false)
                .build();

        emailVerificationCodeRepository.save(verificationCode);
        return code;
    }

    @Override
    @Transactional
    public void deleteUnusedCodes(Long userId) {
        emailVerificationCodeRepository.deleteByUserIdAndUsedFalse(userId);
    }

    @Override
    @Transactional
    public boolean validateAndConsume(Long userId, String code) {
        Optional<EmailVerificationCode> localCode = emailVerificationCodeRepository
                .findByUserIdAndCodeAndUsedFalse(userId, code);

        if (localCode.isPresent()) {
            EmailVerificationCode verificationCode = localCode.get();
            if (verificationCode.isExpired()) {
                throw new AuthenticationException("Verification code has expired. Please request a new one.");
            }
            verificationCode.setUsed(true);
            emailVerificationCodeRepository.save(verificationCode);
            return true;
        }

        try {
            ResponseEntity<Boolean> response = userClient.validateAndConsumeCode(
                    userId, Map.of("code", code));
            return Boolean.TRUE.equals(response.getBody());
        } catch (Exception e) {
            log.error("Error calling validateAndConsumeCode on mcsv-user: {}", e.getMessage());
            return false;
        }
    }
}
