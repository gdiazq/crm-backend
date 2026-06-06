package com.crm.mcsv_auth.mapper;

import com.crm.mcsv_auth.dto.UserSessionDto;
import com.crm.mcsv_auth.entity.UserSession;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public UserSessionDto toDto(UserSession session) {
        return UserSessionDto.builder()
                .id(session.getId())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .deviceId(session.getDeviceId())
                .createdAt(session.getCreatedAt())
                .lastSeenAt(session.getLastSeenAt())
                .expiresAt(session.getExpiresAt())
                .build();
    }
}
