package com.crm.mcsv_auth.service;

public interface EmailVerificationCodeService {

    String createCode(Long userId);

    void deleteUnusedCodes(Long userId);

    boolean validateAndConsume(Long userId, String code);
}
