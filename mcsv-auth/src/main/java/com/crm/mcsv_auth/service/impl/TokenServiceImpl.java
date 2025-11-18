package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.entity.RefreshToken;
import com.crm.mcsv_auth.exception.TokenException;
import com.crm.mcsv_auth.repository.RefreshTokenRepository;
import com.crm.mcsv_auth.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId, String username) {
        log.info("Creating refresh token for user: {}", username);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .username(username)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        log.info("Validating refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Invalid refresh token"));

        if (refreshToken.getRevoked()) {
            throw new TokenException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new TokenException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        log.info("Revoking refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Refresh token not found"));

        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        log.info("Revoking all tokens for user ID: {}", userId);

        List<RefreshToken> userTokens = refreshTokenRepository.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        for (RefreshToken token : userTokens) {
            if (!token.getRevoked()) {
                token.setRevoked(true);
                token.setRevokedAt(now);
            }
        }

        refreshTokenRepository.saveAll(userTokens);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        log.info("Deleting expired refresh tokens");
        refreshTokenRepository.deleteByExpiresAtBeforeAndRevokedFalse(LocalDateTime.now());
    }
}
