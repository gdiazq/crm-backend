package com.crm.mcsv_user.client.fallback;

import com.crm.mcsv_user.client.EmailClient;
import com.crm.common.dto.EmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailClientFallback implements EmailClient {

    @Override
    public ResponseEntity<Void> sendEmail(EmailRequest request) {
        log.warn("mcsv-email no disponible — email no enviado");
        return ResponseEntity.ok().build();
    }
}
