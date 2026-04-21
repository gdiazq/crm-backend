package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.ContractAnnexType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractAnnexTypeRepository extends JpaRepository<ContractAnnexType, Long> {
    Optional<ContractAnnexType> findByName(String name);
    List<ContractAnnexType> findByActiveTrue();
}
