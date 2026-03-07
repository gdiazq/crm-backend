package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContractStatusRepository extends JpaRepository<ContractStatus, Long> {
    Optional<ContractStatus> findByName(String name);
}
