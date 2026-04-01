package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.TerminationAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TerminationAgreementRepository extends JpaRepository<TerminationAgreement, Long>,
        JpaSpecificationExecutor<TerminationAgreement> {

    boolean existsByContractId(Long contractId);

    List<TerminationAgreement> findByEmployeeId(Long employeeId);
}
