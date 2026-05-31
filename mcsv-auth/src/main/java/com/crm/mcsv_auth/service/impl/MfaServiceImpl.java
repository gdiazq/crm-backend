package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.dto.MfaSetupResponse;
import com.crm.mcsv_auth.dto.MfaStatusResponse;
import com.crm.mcsv_auth.entity.UserMfa;
import com.crm.mcsv_auth.exception.AuthenticationException;
import com.crm.mcsv_auth.repository.UserMfaRepository;
import com.crm.mcsv_auth.service.MfaService;
import com.crm.mcsv_auth.service.TotpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MfaServiceImpl implements MfaService {

    private static final String ISSUER = "CRM";

    private final UserMfaRepository userMfaRepository;
    private final TotpService totpService;

    @Override
    @Transactional
    public MfaSetupResponse setupTotp(Long userId, String username) {
        String secret = totpService.generateSecret();
        UserMfa userMfa = userMfaRepository.findByUserId(userId)
                .orElse(UserMfa.builder().userId(userId).build());
        userMfa.setTotpSecret(secret);
        userMfa.setEnabled(false);
        userMfa.setVerifiedAt(null);
        userMfaRepository.save(userMfa);

        String otpauth = totpService.buildOtpAuthUrl(ISSUER, username, secret);

        return MfaSetupResponse.builder()
                .secret(secret)
                .otpauthUrl(otpauth)
                .build();
    }

    @Override
    @Transactional
    public boolean verifyTotp(Long userId, String code) {
        UserMfa userMfa = userMfaRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthenticationException("MFA not configured"));

        boolean valid = totpService.validateCode(userMfa.getTotpSecret(), code);
        if (valid) {
            userMfa.setEnabled(true);
            userMfa.setVerifiedAt(LocalDateTime.now());
            userMfaRepository.save(userMfa);
        }
        return valid;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMfaEnabled(Long userId) {
        return userMfaRepository.findByUserId(userId)
                .map(UserMfa::getEnabled)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public MfaStatusResponse getMfaStatus(Long userId) {
        return userMfaRepository.findByUserId(userId)
                .map(mfa -> MfaStatusResponse.builder()
                        .status(mfa.getEnabled())
                        .verified(mfa.getVerifiedAt() != null)
                        .lastVerification(mfa.getVerifiedAt())
                        .build())
                .orElse(MfaStatusResponse.builder()
                        .status(false)
                        .verified(false)
                        .lastVerification(null)
                        .build());
    }

    @Override
    @Transactional
    public void disableTotp(Long userId) {
        userMfaRepository.findByUserId(userId).ifPresent(mfa -> {
            mfa.setEnabled(false);
            mfa.setVerifiedAt(null);
            userMfaRepository.save(mfa);
        });
    }

}
