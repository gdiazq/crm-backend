package com.crm.mcsv_auth.repository;

import com.crm.mcsv_auth.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode> findByUserIdAndCodeAndUsedFalse(Long userId, String code);

    void deleteByExpiresAtBefore(LocalDateTime date);

    @Modifying
    void deleteByUserIdAndUsedFalse(Long userId);
}
