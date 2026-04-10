package com.crm.mcsv_auth.client;

import com.crm.common.dto.EmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class EmailClientFallback implements EmailClient {

    private static final Logger log = LoggerFactory.getLogger(EmailClientFallback.class);

    @Override
    public ResponseEntity<Void> sendEmail(EmailRequest request) {
        log.warn("Email service unavailable. Email skipped.");
        return ResponseEntity.ok().build();
    }
}
