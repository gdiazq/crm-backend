package com.crm.mcsv_auth.repository;

import com.crm.mcsv_auth.entity.UserMfa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMfaRepository extends JpaRepository<UserMfa, Long> {
    Optional<UserMfa> findByUserId(Long userId);
}
