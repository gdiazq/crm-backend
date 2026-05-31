package com.crm.mcsv_auth.service;

/**
 * Time-based One-Time Password algorithm (RFC 6238) used for MFA / 2FA.
 * Stateless — no persistence — so it can be unit-tested in isolation.
 */
public interface TotpService {

    /** Generates a new Base32 shared secret (seed for the authenticator app). */
    String generateSecret();

    /** Validates a 6-digit code against the secret, tolerating ±1 time window for clock drift. */
    boolean validateCode(String base32Secret, String code);

    /** Builds the otpauth:// provisioning URI (rendered as a QR by the frontend). */
    String buildOtpAuthUrl(String issuer, String accountName, String base32Secret);
}
