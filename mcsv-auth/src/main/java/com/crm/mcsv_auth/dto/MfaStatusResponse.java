package com.crm.mcsv_auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MfaStatusResponse {
    private boolean status;
    private boolean verified;
    private LocalDateTime lastVerification;
}
