package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.exception.AuthenticationException;
import com.crm.mcsv_auth.service.TotpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

@Service
@Slf4j
public class TotpServiceImpl implements TotpService {

    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base32 base32 = new Base32();

    @Override
    public String generateSecret() {
        byte[] bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        return base32.encodeToString(bytes).replace("=", "");
    }

    @Override
    public boolean validateCode(String base32Secret, String code) {
        long timeWindow = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
        for (int i = -1; i <= 1; i++) {
            String candidate = generateCode(base32Secret, timeWindow + i);
            if (candidate.equals(code)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String buildOtpAuthUrl(String issuer, String accountName, String base32Secret) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%d&period=%d",
                issuer, accountName, base32Secret, issuer, CODE_DIGITS, TIME_STEP_SECONDS);
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
