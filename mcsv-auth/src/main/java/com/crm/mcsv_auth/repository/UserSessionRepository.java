package com.crm.mcsv_auth.repository;

import com.crm.mcsv_auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    List<UserSession> findByUserId(Long userId);
    List<UserSession> findByUserIdAndRevokedFalse(Long userId);
    List<UserSession> findByUserIdAndRevokedFalseOrderByLastSeenAtDescCreatedAtDesc(Long userId);
    List<UserSession> findByUserIdAndRevokedFalseAndExpiresAtAfterOrderByLastSeenAtDescCreatedAtDesc(Long userId, LocalDateTime now);
    Optional<UserSession> findByUserIdAndDeviceIdAndRevokedFalse(Long userId, String deviceId);
    Optional<UserSession> findByIdAndUserIdAndRevokedFalse(Long id, Long userId);
    Optional<UserSession> findByIdAndUserIdAndRevokedFalseAndExpiresAtAfter(Long id, Long userId, LocalDateTime now);
    Optional<UserSession> findFirstByUserIdAndRevokedFalseOrderByCreatedAtDesc(Long userId);
}
