package com.crm.mcsv_user.client;

import com.crm.mcsv_user.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mcsv-email")
public interface EmailClient {

    @PostMapping("/send")
    ResponseEntity<Void> sendEmail(@RequestBody EmailRequest request);
}
