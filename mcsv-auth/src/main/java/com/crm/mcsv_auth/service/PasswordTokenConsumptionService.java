package com.crm.mcsv_auth.service;

import com.crm.mcsv_auth.dto.ResetPasswordRequest;

public interface PasswordTokenConsumptionService {

    void consume(ResetPasswordRequest request);
}
