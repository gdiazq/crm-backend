package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.EmployeeLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeLeaveRepository extends JpaRepository<EmployeeLeave, Long>, JpaSpecificationExecutor<EmployeeLeave> {
    List<EmployeeLeave> findByEmployeeId(Long employeeId);
    List<EmployeeLeave> findByContractId(Long contractId);

    @Query(value = """
            SELECT el.id
            FROM employee_leaves el
            WHERE el.employee_id = :employeeId
              AND :date BETWEEN el.start_date AND el.end_date
              AND (
                SELECT es.name
                FROM hr_requests hr
                JOIN employee_statuses es ON es.id = hr.status_id
                WHERE hr.leave_id = el.id
                ORDER BY hr.created_at DESC
                LIMIT 1
              ) = 'Aprobado'
            LIMIT 1
            """, nativeQuery = true)
    Optional<Long> findApprovedLeaveIdCoveringDate(@Param("employeeId") Long employeeId,
                                                   @Param("date") LocalDate date);
}
