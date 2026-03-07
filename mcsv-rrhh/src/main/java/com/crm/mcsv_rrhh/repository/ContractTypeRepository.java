package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContractTypeRepository extends JpaRepository<ContractType, Long> {
    Optional<ContractType> findByName(String name);
}
