package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {
    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    void deleteByGeneratedByLeaveIdAndManuallyOverriddenFalse(Long generatedByLeaveId);
    long countByStatusId(Long statusId);
}
