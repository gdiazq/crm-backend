package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {
    List<Contract> findByEmployeeId(Long employeeId);
    boolean existsByEmployeeIdAndStatusId(Long employeeId, Long statusId);
    long countByStatusId(Long statusId);
    long countByEmployeeIdAndStatusId(Long employeeId, Long statusId);
}
