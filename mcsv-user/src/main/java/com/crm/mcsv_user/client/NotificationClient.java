package com.crm.mcsv_user.client;

import com.crm.mcsv_user.dto.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mcsv-notification")
public interface NotificationClient {

    @PostMapping("/send")
    ResponseEntity<Void> send(@RequestBody SendNotificationRequest request);
}
