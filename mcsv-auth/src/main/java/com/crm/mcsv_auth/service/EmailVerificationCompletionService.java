package com.crm.mcsv_auth.service;

import java.util.Map;

public interface EmailVerificationCompletionService {

    Map<String, String> complete(Long userId);
}
