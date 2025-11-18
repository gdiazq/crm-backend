package com.crm.mcsv_auth.service;

import com.crm.mcsv_auth.entity.RefreshToken;

public interface TokenService {

    RefreshToken createRefreshToken(Long userId, String username);

    RefreshToken validateRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeAllUserTokens(Long userId);

    void deleteExpiredTokens();
}
