package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.ContractAnnex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ContractAnnexRepository extends JpaRepository<ContractAnnex, Long>, JpaSpecificationExecutor<ContractAnnex> {
    List<ContractAnnex> findByEmployeeId(Long employeeId);
    List<ContractAnnex> findByContractId(Long contractId);
}
