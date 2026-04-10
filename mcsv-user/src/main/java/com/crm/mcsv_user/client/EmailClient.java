package com.crm.mcsv_user.client;

import com.crm.common.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mcsv-email", fallback = com.crm.mcsv_user.client.fallback.EmailClientFallback.class)
public interface EmailClient {

    @PostMapping("/send")
    ResponseEntity<Void> sendEmail(@RequestBody EmailRequest request);
}
