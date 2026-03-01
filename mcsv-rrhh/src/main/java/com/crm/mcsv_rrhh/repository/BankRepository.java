package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional<Bank> findByName(String name);
}
