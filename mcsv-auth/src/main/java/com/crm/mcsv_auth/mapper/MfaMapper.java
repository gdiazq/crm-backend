package com.crm.mcsv_auth.mapper;

import com.crm.mcsv_auth.dto.MfaSetupResponse;
import com.crm.mcsv_auth.dto.MfaStatusResponse;
import com.crm.mcsv_auth.entity.UserMfa;
import org.springframework.stereotype.Component;

@Component
public class MfaMapper {

    public MfaSetupResponse toSetupResponse(String secret, String otpauthUrl) {
        return MfaSetupResponse.builder()
                .secret(secret)
                .otpauthUrl(otpauthUrl)
                .build();
    }

    public MfaStatusResponse toStatusResponse(UserMfa mfa) {
        return MfaStatusResponse.builder()
                .status(mfa.getEnabled())
                .verified(mfa.getVerifiedAt() != null)
                .lastVerification(mfa.getVerifiedAt())
                .build();
    }

    public MfaStatusResponse emptyStatus() {
        return MfaStatusResponse.builder()
                .status(false)
                .verified(false)
                .lastVerification(null)
                .build();
    }
}
