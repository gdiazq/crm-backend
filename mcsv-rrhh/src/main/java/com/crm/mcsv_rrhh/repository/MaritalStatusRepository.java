package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.MaritalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaritalStatusRepository extends JpaRepository<MaritalStatus, Long> {
    Optional<MaritalStatus> findByName(String name);
}
