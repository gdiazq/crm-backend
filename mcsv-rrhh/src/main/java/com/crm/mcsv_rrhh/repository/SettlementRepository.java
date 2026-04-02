package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long>,
        JpaSpecificationExecutor<Settlement> {

    boolean existsByContractId(Long contractId);

    List<Settlement> findByEmployeeId(Long employeeId);
}
