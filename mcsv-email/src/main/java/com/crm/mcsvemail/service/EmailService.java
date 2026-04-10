package com.crm.mcsvemail.service;

import com.crm.common.dto.EmailRequest;

public interface EmailService {

    void sendEmail(EmailRequest request);
}
