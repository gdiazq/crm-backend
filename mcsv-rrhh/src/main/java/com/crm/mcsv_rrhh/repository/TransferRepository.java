package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long>, JpaSpecificationExecutor<Transfer> {
    List<Transfer> findByEmployeeId(Long employeeId);
}
