package com.crm.mcsv_user.repository;

import com.crm.mcsv_user.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode> findByUserIdAndCodeAndUsedFalse(Long userId, String code);

    @Modifying
    void deleteByUserIdAndUsedFalse(Long userId);
}
