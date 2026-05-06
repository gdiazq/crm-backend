package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.AttendanceMark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface AttendanceMarkRepository extends JpaRepository<AttendanceMark, Long> {
    boolean existsByAttendanceIdAndMarkType(Long attendanceId, String markType);
    boolean existsByAttendanceIdAndMarkTypeAndIdNot(Long attendanceId, String markType, Long id);
    List<AttendanceMark> findByAttendanceIdOrderByMarkTimeAsc(Long attendanceId);
    List<AttendanceMark> findByEmployeeIdOrderByMarkTimeDesc(Long employeeId);
    List<AttendanceMark> findByCostCenterOrderByMarkTimeDesc(Integer costCenter);
    Optional<AttendanceMark> findFirstByAttendanceIdAndMarkTypeOrderByMarkTimeAsc(Long attendanceId, String markType);
    Optional<AttendanceMark> findFirstByAttendanceIdAndMarkTypeOrderByMarkTimeDesc(Long attendanceId, String markType);
}
