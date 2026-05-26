package com.crm.mcsv_auth.util;

import java.security.SecureRandom;
import java.util.UUID;

public final class AuthCredentialUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private AuthCredentialUtil() {
    }

    public static String generateVerificationCode() {
        int code = SECURE_RANDOM.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    public static String generatePlaceholderPassword() {
        return "Tmp!" + UUID.randomUUID();
    }
}
