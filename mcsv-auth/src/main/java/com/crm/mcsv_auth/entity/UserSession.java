package com.crm.mcsv_auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions", indexes = {
    @Index(name = "idx_user_session_user_id", columnList = "user_id"),
    @Index(name = "idx_user_session_user_revoked_expires", columnList = "user_id, revoked, expires_at"),
    @Index(name = "idx_user_session_user_device_revoked", columnList = "user_id, device_id, revoked")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "device_id", length = 128)
    private String deviceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastSeenAt = createdAt;
        if (expiresAt == null) {
            expiresAt = createdAt.plusDays(7);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastSeenAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
