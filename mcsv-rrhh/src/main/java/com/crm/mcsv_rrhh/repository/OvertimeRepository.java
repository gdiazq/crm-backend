package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Overtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OvertimeRepository extends JpaRepository<Overtime, Long>, JpaSpecificationExecutor<Overtime> {

    List<Overtime> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    @Query(value = """
            SELECT o.*
            FROM overtimes o
            WHERE o.attendance_id = :attendanceId
              AND (
                SELECT es.name
                FROM hr_requests hr
                JOIN employee_statuses es ON es.id = hr.status_id
                WHERE hr.overtime_id = o.id
                ORDER BY hr.created_at DESC
                LIMIT 1
              ) = 'Aprobado'
            """, nativeQuery = true)
    List<Overtime> findApprovedByAttendanceId(@Param("attendanceId") Long attendanceId);

    @Query(value = """
            SELECT o.*
            FROM overtimes o
            WHERE o.employee_id = :employeeId
              AND o.date BETWEEN :from AND :to
              AND (
                SELECT es.name
                FROM hr_requests hr
                JOIN employee_statuses es ON es.id = hr.status_id
                WHERE hr.overtime_id = o.id
                ORDER BY hr.created_at DESC
                LIMIT 1
              ) = 'Aprobado'
            """, nativeQuery = true)
    List<Overtime> findApprovedByEmployeeIdAndDateBetween(@Param("employeeId") Long employeeId,
                                                         @Param("from") LocalDate from,
                                                         @Param("to") LocalDate to);
}
