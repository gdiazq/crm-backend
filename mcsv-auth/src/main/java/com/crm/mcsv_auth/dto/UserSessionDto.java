package com.crm.mcsv_auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSessionDto {
    private Long id;
    private String ipAddress;
    private String userAgent;
    private String deviceId;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeenAt;
}
