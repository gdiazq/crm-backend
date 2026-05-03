package com.crm.mcsv_auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_token_session_id", columnList = "session_id"),
    @Index(name = "idx_refresh_token_session_revoked", columnList = "session_id, revoked"),
    @Index(name = "idx_refresh_token_session_expires", columnList = "session_id, expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_refresh_token_session"))
    private UserSession session;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Transient
    private String plainToken;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
