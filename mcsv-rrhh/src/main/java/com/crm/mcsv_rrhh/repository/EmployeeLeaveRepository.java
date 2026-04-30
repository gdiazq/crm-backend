package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.EmployeeLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EmployeeLeaveRepository extends JpaRepository<EmployeeLeave, Long>, JpaSpecificationExecutor<EmployeeLeave> {
    List<EmployeeLeave> findByEmployeeId(Long employeeId);
    List<EmployeeLeave> findByContractId(Long contractId);
}
