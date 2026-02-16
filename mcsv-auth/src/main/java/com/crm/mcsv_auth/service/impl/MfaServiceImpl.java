package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.dto.MfaSetupResponse;
import com.crm.mcsv_auth.dto.MfaStatusResponse;
import com.crm.mcsv_auth.entity.UserMfa;
import com.crm.mcsv_auth.exception.AuthenticationException;
import com.crm.mcsv_auth.repository.UserMfaRepository;
import com.crm.mcsv_auth.service.MfaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MfaServiceImpl implements MfaService {

    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final String ISSUER = "CRM";

    private final UserMfaRepository userMfaRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Base32 base32 = new Base32();

    @Override
    @Transactional
    public MfaSetupResponse setupTotp(Long userId, String username) {
        String secret = generateSecret();
        UserMfa userMfa = userMfaRepository.findByUserId(userId)
                .orElse(UserMfa.builder().userId(userId).build());
        userMfa.setTotpSecret(secret);
        userMfa.setEnabled(false);
        userMfa.setVerifiedAt(null);
        userMfaRepository.save(userMfa);

        String otpauth = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%d&period=%d",
                ISSUER, username, secret, ISSUER, CODE_DIGITS, TIME_STEP_SECONDS);

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

        boolean valid = validateCode(userMfa.getTotpSecret(), code);
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

    private String generateSecret() {
        byte[] bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        return base32.encodeToString(bytes).replace("=", "");
    }

    private boolean validateCode(String base32Secret, String code) {
        long timeWindow = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
        for (int i = -1; i <= 1; i++) {
            String candidate = generateCode(base32Secret, timeWindow + i);
            if (candidate.equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String generateCode(String base32Secret, long timeWindow) {
        try {
            byte[] key = base32.decode(base32Secret);
            ByteBuffer buffer = ByteBuffer.allocate(8).putLong(timeWindow);
            byte[] counter = buffer.array();

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(counter);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary =
                    ((hash[offset] & 0x7f) << 24) |
                    ((hash[offset + 1] & 0xff) << 16) |
                    ((hash[offset + 2] & 0xff) << 8) |
                    (hash[offset + 3] & 0xff);

            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", otp);
        } catch (Exception e) {
            log.error("Error generating TOTP code", e);
            throw new AuthenticationException("Invalid TOTP configuration");
        }
    }
}
