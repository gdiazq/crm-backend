package com.crm.mcsv_auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationRequest {

    private Long userId;
    private String title;
    private String message;
    @Builder.Default
    private String type = "INFO";
}
