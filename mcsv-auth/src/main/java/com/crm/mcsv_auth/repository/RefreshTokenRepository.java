package com.crm.mcsv_auth.repository;

import com.crm.mcsv_auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(Long userId);

    void deleteByToken(String token);

    void deleteByUserId(Long userId);

    void deleteByExpiresAtBeforeAndRevokedFalse(LocalDateTime date);
}
