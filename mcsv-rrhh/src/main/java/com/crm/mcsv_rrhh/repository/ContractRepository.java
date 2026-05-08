package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {
    List<Contract> findByEmployeeId(Long employeeId);

    @Query("""
            SELECT c
            FROM Contract c
            WHERE (c.startDate BETWEEN :from AND :to)
               OR (c.endDate BETWEEN :from AND :to)
            """)
    List<Contract> findCalendarEvents(@Param("from") LocalDate from, @Param("to") LocalDate to);

    boolean existsByEmployeeIdAndStatusId(Long employeeId, Long statusId);
    long countByStatusId(Long statusId);
    long countByEmployeeIdAndStatusId(Long employeeId, Long statusId);
    long countByEmployeeId(Long employeeId);
    long countByContractStatusId(Long contractStatusId);
    long countByEmployeeIdAndContractStatusId(Long employeeId, Long contractStatusId);
}
