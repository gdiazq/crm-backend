package com.crm.mcsv_auth.service;

import com.crm.mcsv_auth.dto.MfaSetupResponse;

public interface MfaService {
    MfaSetupResponse setupTotp(Long userId, String username);
    boolean verifyTotp(Long userId, String code);
    boolean isMfaEnabled(Long userId);
    void disableTotp(Long userId);
}
