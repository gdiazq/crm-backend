package com.crm.mcsv_auth.repository;

import com.crm.mcsv_auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    List<UserSession> findByUserId(Long userId);
    List<UserSession> findByUserIdAndRevokedFalse(Long userId);
    Optional<UserSession> findByUserIdAndDeviceIdAndRevokedFalse(Long userId, String deviceId);
}
