package com.crm.mcsv_auth.service;

import com.crm.mcsv_auth.dto.MfaSetupResponse;
import com.crm.mcsv_auth.dto.MfaStatusResponse;

public interface MfaService {
    MfaSetupResponse setupTotp(Long userId, String username);
    boolean verifyTotp(Long userId, String code);
    boolean isMfaEnabled(Long userId);
    MfaStatusResponse getMfaStatus(Long userId);
    void disableTotp(Long userId);
}
