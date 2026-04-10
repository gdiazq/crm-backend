package com.crm.mcsv_auth.client;

import com.crm.common.dto.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mcsv-notification", contextId = "authNotificationClient", fallback = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping("/send")
    ResponseEntity<Void> send(@RequestBody SendNotificationRequest request);
}
