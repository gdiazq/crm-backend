package com.crm.mcsv_auth.client;

import com.crm.common.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mcsv-email", contextId = "authEmailClient", fallback = EmailClientFallback.class)
public interface EmailClient {

    @PostMapping("/send")
    ResponseEntity<Void> sendEmail(@RequestBody EmailRequest request);
}
