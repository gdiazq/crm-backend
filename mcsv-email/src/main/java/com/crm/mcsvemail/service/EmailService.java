package com.crm.mcsvemail.service;

import com.crm.mcsvemail.dto.EmailRequest;

public interface EmailService {

    void sendEmail(EmailRequest request);
}
